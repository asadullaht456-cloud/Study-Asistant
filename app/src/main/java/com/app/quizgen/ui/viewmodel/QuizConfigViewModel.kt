package com.app.quizgen.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.quizgen.data.local.entity.StudyMaterialEntity
import com.app.quizgen.data.repository.StudyMaterialRepository
import com.app.quizgen.domain.usecase.GenerateQuizParams
import com.app.quizgen.domain.usecase.GenerateQuizUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Generation UI status states for Screen 3.
 */
sealed interface QuizGenerationUiState {
    data object Idle : QuizGenerationUiState
    data class Loading(val message: String = "Extracting text & generating questions...") : QuizGenerationUiState
    data class Success(val quizId: Long) : QuizGenerationUiState
    data class Error(val message: String) : QuizGenerationUiState
}

/**
 * ViewModel for Screen 3 (Quiz Configuration Screen).
 *
 * Manages material chips, 3-way difficulty selection, question counters,
 * total question validation, and AI generation invocation.
 *
 * Implements Task 5.4 from dev_work_phases.md.
 */
@HiltViewModel
class QuizConfigViewModel @Inject constructor(
    private val studyMaterialRepository: StudyMaterialRepository,
    private val generateQuizUseCase: GenerateQuizUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _selectedMaterials = MutableStateFlow<List<StudyMaterialEntity>>(emptyList())
    val selectedMaterials: StateFlow<List<StudyMaterialEntity>> = _selectedMaterials.asStateFlow()

    private val _difficulty = MutableStateFlow("MEDIUM")
    val difficulty: StateFlow<String> = _difficulty.asStateFlow()

    private val _mcqCount = MutableStateFlow(10)
    val mcqCount: StateFlow<Int> = _mcqCount.asStateFlow()

    private val _shortQCount = MutableStateFlow(3)
    val shortQCount: StateFlow<Int> = _shortQCount.asStateFlow()

    private val _longQCount = MutableStateFlow(1)
    val longQCount: StateFlow<Int> = _longQCount.asStateFlow()

    private val _generationState = MutableStateFlow<QuizGenerationUiState>(QuizGenerationUiState.Idle)
    val generationState: StateFlow<QuizGenerationUiState> = _generationState.asStateFlow()

    /**
     * Dynamically computed total questions count banner.
     */
    val totalQuestions: StateFlow<Int> = combine(
        _mcqCount,
        _shortQCount,
        _longQCount
    ) { mcq, shortQ, longQ ->
        mcq + shortQ + longQ
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 14
    )

    /**
     * Form validity: At least 1 material selected AND total questions > 0.
     */
    val isValidConfig: StateFlow<Boolean> = combine(
        _selectedMaterials,
        totalQuestions
    ) { materials, total ->
        materials.isNotEmpty() && total > 0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        // Load material IDs if passed via SavedStateHandle navigation arguments
        val materialIdsArg = savedStateHandle.get<LongArray>("materialIds")
            ?: savedStateHandle.get<List<Long>>("materialIds")?.toLongArray()

        if (materialIdsArg != null && materialIdsArg.isNotEmpty()) {
            loadSelectedMaterials(materialIdsArg.toList())
        }
    }

    /**
     * Loads study materials by IDs for the summary chips.
     */
    fun loadSelectedMaterials(materialIds: List<Long>) {
        viewModelScope.launch {
            val entities = studyMaterialRepository.getMaterialsByIds(materialIds)
            _selectedMaterials.value = entities
        }
    }

    /**
     * Directly sets selected materials.
     */
    fun setSelectedMaterials(materials: List<StudyMaterialEntity>) {
        _selectedMaterials.value = materials
    }

    /**
     * Removes a material chip from the configuration.
     */
    fun removeMaterial(materialId: Long) {
        _selectedMaterials.value = _selectedMaterials.value.filter { it.material_id != materialId }
    }

    /**
     * Sets quiz difficulty: "EASY", "MEDIUM", or "HARD".
     */
    fun setDifficulty(diff: String) {
        val normalized = diff.uppercase().trim()
        if (normalized in listOf("EASY", "MEDIUM", "HARD")) {
            _difficulty.value = normalized
        }
    }

    // --- Counter Handlers (MCQ) ---
    fun incrementMcq() {
        _mcqCount.value = _mcqCount.value + 1
    }

    fun decrementMcq() {
        if (_mcqCount.value > 0) {
            _mcqCount.value = _mcqCount.value - 1
        }
    }

    fun setMcqCount(count: Int) {
        _mcqCount.value = count.coerceAtLeast(0)
    }

    // --- Counter Handlers (Short Qs) ---
    fun incrementShortQ() {
        _shortQCount.value = _shortQCount.value + 1
    }

    fun decrementShortQ() {
        if (_shortQCount.value > 0) {
            _shortQCount.value = _shortQCount.value - 1
        }
    }

    fun setShortQCount(count: Int) {
        _shortQCount.value = count.coerceAtLeast(0)
    }

    // --- Counter Handlers (Long Qs) ---
    fun incrementLongQ() {
        _longQCount.value = _longQCount.value + 1
    }

    fun decrementLongQ() {
        if (_longQCount.value > 0) {
            _longQCount.value = _longQCount.value - 1
        }
    }

    fun setLongQCount(count: Int) {
        _longQCount.value = count.coerceAtLeast(0)
    }

    /**
     * Invokes GenerateQuizUseCase with the configured parameters.
     * Displays non-cancellable loading state during execution.
     *
     * @param customTitle Optional custom title for the quiz.
     */
    fun generateQuiz(customTitle: String? = null) {
        val materials = _selectedMaterials.value
        if (materials.isEmpty()) {
            _generationState.value = QuizGenerationUiState.Error("Please select at least one study material.")
            return
        }

        val total = totalQuestions.value
        if (total <= 0) {
            _generationState.value = QuizGenerationUiState.Error("Total questions must be greater than zero.")
            return
        }

        viewModelScope.launch {
            // Strict loading text from ui_ux_requirements_specifications.md Screen 3
            _generationState.value = QuizGenerationUiState.Loading("Extracting text & generating questions...")

            val params = GenerateQuizParams(
                materialIds = materials.map { it.material_id },
                difficulty = _difficulty.value,
                mcqCount = _mcqCount.value,
                shortQCount = _shortQCount.value,
                longQCount = _longQCount.value,
                customTitle = customTitle
            )

            val result = generateQuizUseCase(params)

            result.fold(
                onSuccess = { quizId ->
                    _generationState.value = QuizGenerationUiState.Success(quizId)
                },
                onFailure = { error ->
                    _generationState.value = QuizGenerationUiState.Error(
                        error.message ?: "Quiz generation failed. Please try again."
                    )
                }
            )
        }
    }

    /**
     * Resets generation status back to Idle.
     */
    fun resetGenerationState() {
        _generationState.value = QuizGenerationUiState.Idle
    }
}
