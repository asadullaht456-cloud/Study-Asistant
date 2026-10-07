package com.app.quizgen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.quizgen.data.local.entity.QuizEntity
import com.app.quizgen.data.local.entity.StudyMaterialEntity
import com.app.quizgen.data.repository.QuizRepository
import com.app.quizgen.data.repository.StudyMaterialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dashboard screen tabs per ui_ux_requirements_specifications.md Screen 1.
 */
enum class DashboardTab {
    GENERATED_QUIZZES,
    SAVED_MATERIALS
}

/**
 * ViewModel for Screen 1 (Dashboard / Home Screen).
 *
 * Exposes reactive StateFlows for previously generated quizzes and
 * saved library study materials, manages active tab selection,
 * and handles quiz deletion with cascading.
 *
 * Implements Task 5.2 from dev_work_phases.md.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val studyMaterialRepository: StudyMaterialRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(DashboardTab.GENERATED_QUIZZES)
    val selectedTab: StateFlow<DashboardTab> = _selectedTab.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    /**
     * Hot StateFlow of all generated quizzes ordered by created_at DESC.
     */
    val quizzes: StateFlow<List<QuizEntity>> = quizRepository.getAllQuizzes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Hot StateFlow of all uploaded study materials ordered by uploaded_at DESC.
     */
    val materials: StateFlow<List<StudyMaterialEntity>> = studyMaterialRepository.getAllMaterials()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Selects the active dashboard tab (Generated Quizzes vs Saved Materials).
     */
    fun selectTab(tab: DashboardTab) {
        _selectedTab.value = tab
    }

    /**
     * Deletes a quiz by its ID. Cascades to questions and cross-references.
     */
    fun deleteQuiz(quizId: Long) {
        viewModelScope.launch {
            try {
                quizRepository.deleteQuizById(quizId)
                _userMessage.value = "Quiz deleted successfully."
            } catch (e: Exception) {
                _userMessage.value = "Failed to delete quiz: ${e.message}"
            }
        }
    }

    /**
     * Deletes a study material by its ID.
     */
    fun deleteMaterial(materialId: Long) {
        viewModelScope.launch {
            try {
                studyMaterialRepository.deleteMaterialById(materialId)
                _userMessage.value = "Material deleted successfully."
            } catch (e: Exception) {
                _userMessage.value = "Failed to delete material: ${e.message}"
            }
        }
    }

    /**
     * Clears the transient user message.
     */
    fun clearUserMessage() {
        _userMessage.value = null
    }
}
