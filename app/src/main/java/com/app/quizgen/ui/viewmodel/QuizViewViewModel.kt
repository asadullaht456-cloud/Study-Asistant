package com.app.quizgen.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.quizgen.data.local.entity.QuestionEntity
import com.app.quizgen.data.local.relation.QuizWithQuestionsAndMaterials
import com.app.quizgen.data.repository.QuizRepository
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
 * Filter categories for Screen 4 per ui_ux_requirements_specifications.md.
 * Exactly these 4 options: [ All ], [ MCQs ], [ Short Qs ], [ Long Qs ].
 */
enum class QuestionFilter(val label: String) {
    ALL("All"),
    MCQ("MCQs"),
    SHORT("Short Qs"),
    LONG("Long Qs")
}

/**
 * ViewModel for Screen 4 (Quiz & Answer Key View Screen).
 *
 * Loads relational quiz data (Quiz + Questions + Source Materials),
 * maintains sticky filter chip state, and exposes filtered question streams.
 *
 * Implements Task 5.5 from dev_work_phases.md.
 */
@HiltViewModel
class QuizViewViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _quizDetails = MutableStateFlow<QuizWithQuestionsAndMaterials?>(null)
    val quizDetails: StateFlow<QuizWithQuestionsAndMaterials?> = _quizDetails.asStateFlow()

    private val _selectedFilter = MutableStateFlow(QuestionFilter.ALL)
    val selectedFilter: StateFlow<QuestionFilter> = _selectedFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Questions filtered according to the active QuestionFilter chip.
     */
    val filteredQuestions: StateFlow<List<QuestionEntity>> = combine(
        _quizDetails,
        _selectedFilter
    ) { details, filter ->
        val questions = details?.questions ?: emptyList()
        when (filter) {
            QuestionFilter.ALL -> questions
            QuestionFilter.MCQ -> questions.filter { it.questionType == "MCQ" }
            QuestionFilter.SHORT -> questions.filter { it.questionType == "SHORT" }
            QuestionFilter.LONG -> questions.filter { it.questionType == "LONG" }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Auto-load quiz if quizId is provided in navigation arguments
        val quizIdArg = savedStateHandle.get<Long>("quizId")
            ?: savedStateHandle.get<String>("quizId")?.toLongOrNull()

        if (quizIdArg != null && quizIdArg > 0) {
            loadQuiz(quizIdArg)
        }
    }

    /**
     * Loads full relational details for a quiz by ID.
     */
    fun loadQuiz(quizId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = quizRepository.getQuizWithDetails(quizId)
                if (result == null) {
                    _errorMessage.value = "Quiz with ID $quizId not found."
                } else {
                    _quizDetails.value = result
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load quiz: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates the active question category filter.
     */
    fun setFilter(filter: QuestionFilter) {
        _selectedFilter.value = filter
    }

    /**
     * Clears error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
