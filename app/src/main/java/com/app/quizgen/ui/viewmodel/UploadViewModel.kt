package com.app.quizgen.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.quizgen.data.local.entity.StudyMaterialEntity
import com.app.quizgen.data.parser.FileParserFactory
import com.app.quizgen.data.storage.FileStorageManager
import com.app.quizgen.data.repository.StudyMaterialRepository
import com.app.quizgen.util.FileValidationResult
import com.app.quizgen.util.FileValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State representing file upload and text extraction progress.
 */
sealed interface UploadUiState {
    data object Idle : UploadUiState
    data object Validating : UploadUiState
    data class ExtractingText(val fileName: String) : UploadUiState
    data class Success(val materialId: Long, val message: String) : UploadUiState
    data class Error(val message: String) : UploadUiState
}

/**
 * ViewModel for Screen 2 (Upload & Select Material Screen).
 *
 * Handles file picking, 15MB constraint validation, text extraction,
 * local storage copy, database persistence, and multi-select library management.
 *
 * Implements Task 5.3 from dev_work_phases.md.
 */
@HiltViewModel
class UploadViewModel @Inject constructor(
    private val fileStorageManager: FileStorageManager,
    private val fileParserFactory: FileParserFactory,
    private val studyMaterialRepository: StudyMaterialRepository
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uploadState: StateFlow<UploadUiState> = _uploadState.asStateFlow()

    private val _selectedMaterialIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedMaterialIds: StateFlow<Set<Long>> = _selectedMaterialIds.asStateFlow()

    /**
     * Hot StateFlow of all saved materials for the multi-select library list.
     */
    val materials: StateFlow<List<StudyMaterialEntity>> = studyMaterialRepository.getAllMaterials()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Handles file selection from system file picker.
     * Enforces the 15MB limit and dispatches parsing + internal persistence.
     *
     * @param contentUri The content:// URI received from the file picker.
     */
    fun handleFileSelected(contentUri: Uri) {
        viewModelScope.launch {
            _uploadState.value = UploadUiState.Validating

            val fileName = fileStorageManager.getFileName(contentUri)
            val fileSizeBytes = fileStorageManager.getFileSize(contentUri)

            if (fileSizeBytes < 0) {
                _uploadState.value = UploadUiState.Error("Unable to access selected file.")
                return@launch
            }

            // 1. Validate file size (<= 15MB) and type (PDF, DOCX, TXT) via FileValidator
            when (val validation = FileValidator.validateFile(fileName, fileSizeBytes)) {
                is FileValidationResult.FileTooLarge -> {
                    // Strict requirement from ui_ux_requirements_specifications.md:
                    // "File size exceeds 15MB limit"
                    _uploadState.value = UploadUiState.Error("File size exceeds 15MB limit")
                    return@launch
                }
                is FileValidationResult.UnsupportedFormat -> {
                    _uploadState.value = UploadUiState.Error(
                        "Unsupported file format: .${validation.extension}. Supported: PDF, DOCX, TXT"
                    )
                    return@launch
                }
                is FileValidationResult.Valid -> {
                    val sizeMb = validation.fileSizeMb
                    val fileType = fileName.substringAfterLast('.', "").uppercase()

                    _uploadState.value = UploadUiState.ExtractingText(fileName)

                    try {
                        // 2. Copy file to app-internal storage
                        val localFileUri = fileStorageManager.saveFile(contentUri, fileName)

                        // 3. Extract text from saved file
                        val inputStream = fileStorageManager.openFile(localFileUri)
                        val extractedText = inputStream.use { stream ->
                            fileParserFactory.extractText(fileType, stream)
                        }

                        if (extractedText.isBlank()) {
                            _uploadState.value = UploadUiState.Error(
                                "No readable text could be extracted from $fileName."
                            )
                            return@launch
                        }

                        // 4. Persist StudyMaterialEntity in Room DB
                        val entity = StudyMaterialEntity(
                            material_id = 0,
                            title = fileName,
                            file_type = fileType,
                            file_uri = localFileUri,
                            extracted_text = extractedText,
                            file_size_mb = sizeMb,
                            uploaded_at = System.currentTimeMillis()
                        )
                        val newId = studyMaterialRepository.insertMaterial(entity)

                        // 5. Auto-select the newly added material
                        _selectedMaterialIds.value = _selectedMaterialIds.value + newId

                        _uploadState.value = UploadUiState.Success(
                            materialId = newId,
                            message = "$fileName uploaded and parsed successfully."
                        )
                    } catch (e: Exception) {
                        _uploadState.value = UploadUiState.Error(
                            "Failed to process file: ${e.message ?: "Unknown error"}"
                        )
                    }
                }
            }
        }
    }

    /**
     * Toggles selection state for a study material in the library list.
     */
    fun toggleMaterialSelection(materialId: Long) {
        val current = _selectedMaterialIds.value
        _selectedMaterialIds.value = if (current.contains(materialId)) {
            current - materialId
        } else {
            current + materialId
        }
    }

    /**
     * Clears all selected materials.
     */
    fun clearSelection() {
        _selectedMaterialIds.value = emptySet()
    }

    /**
     * Selects all available materials currently loaded in the library.
     */
    fun selectAll() {
        _selectedMaterialIds.value = materials.value.map { it.material_id }.toSet()
    }

    /**
     * Resets the upload UI state back to Idle.
     */
    fun resetUploadState() {
        _uploadState.value = UploadUiState.Idle
    }
}
