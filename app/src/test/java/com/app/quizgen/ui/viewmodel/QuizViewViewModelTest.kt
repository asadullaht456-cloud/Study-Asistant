package com.app.quizgen.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.app.quizgen.data.local.entity.QuestionEntity
import com.app.quizgen.data.local.entity.QuizEntity
import com.app.quizgen.data.local.entity.StudyMaterialEntity
import com.app.quizgen.data.local.relation.QuizWithQuestionsAndMaterials
import com.app.quizgen.data.repository.QuizRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeQuizRepo: FakeQuizRepository
    private lateinit var viewModel: QuizViewViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeQuizRepo = FakeQuizRepository()
        viewModel = QuizViewViewModel(fakeQuizRepo, SavedStateHandle())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default filter is ALL`() {
        assertEquals(QuestionFilter.ALL, viewModel.selectedFilter.value)
    }

    @Test
    fun `loadQuiz sets quizDetails on success`() = runTest {
        val sampleQuiz = createSampleQuizDetails(1L)
        fakeQuizRepo.quizToReturn = sampleQuiz

        viewModel.loadQuiz(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.quizDetails.value)
        assertEquals("Sample Quiz", viewModel.quizDetails.value?.quiz?.title)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `loadQuiz sets error message when quiz not found`() = runTest {
        fakeQuizRepo.quizToReturn = null

        viewModel.loadQuiz(999L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.quizDetails.value)
        assertTrue(viewModel.errorMessage.value?.contains("not found") == true)
    }

    @Test
    fun `filter chips properly filter question stream`() = runTest {
        val sampleQuiz = createSampleQuizDetails(1L)
        fakeQuizRepo.quizToReturn = sampleQuiz

        viewModel.loadQuiz(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. ALL filter -> all 3 questions
        viewModel.setFilter(QuestionFilter.ALL)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, viewModel.filteredQuestions.value.size)

        // 2. MCQ filter -> 1 question
        viewModel.setFilter(QuestionFilter.MCQ)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredQuestions.value.size)
        assertEquals("MCQ", viewModel.filteredQuestions.value.first().questionType)

        // 3. SHORT filter -> 1 question
        viewModel.setFilter(QuestionFilter.SHORT)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredQuestions.value.size)
        assertEquals("SHORT", viewModel.filteredQuestions.value.first().questionType)

        // 4. LONG filter -> 1 question
        viewModel.setFilter(QuestionFilter.LONG)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredQuestions.value.size)
        assertEquals("LONG", viewModel.filteredQuestions.value.first().questionType)
    }

    private fun createSampleQuizDetails(quizId: Long): QuizWithQuestionsAndMaterials {
        return QuizWithQuestionsAndMaterials(
            quiz = QuizEntity(
                quizId = quizId,
                title = "Sample Quiz",
                difficultyLevel = "MEDIUM",
                totalQuestions = 3,
                mcqCount = 1,
                shortQCount = 1,
                longQCount = 1,
                createdAt = 1000L
            ),
            questions = listOf(
                QuestionEntity(1, quizId, "MCQ", "MCQ Q?", "[\"A\",\"B\"]", "A", "Expl A"),
                QuestionEntity(2, quizId, "SHORT", "Short Q?", null, "Ans", "Expl B"),
                QuestionEntity(3, quizId, "LONG", "Long Q?", null, "Ans", "Expl C")
            ),
            sourceMaterials = listOf(
                StudyMaterialEntity(1, "Doc.pdf", "PDF", "file:///doc.pdf", "Content", 1.0, 1000L)
            )
        )
    }

    private class FakeQuizRepository : QuizRepository {
        var quizToReturn: QuizWithQuestionsAndMaterials? = null

        override suspend fun createQuizWithQuestionsAndMaterials(
            quiz: QuizEntity,
            questions: List<QuestionEntity>,
            materialIds: List<Long>
        ): Long = 1L

        override fun getAllQuizzes(): Flow<List<QuizEntity>> = emptyFlow()
        override suspend fun getQuizById(quizId: Long): QuizEntity? = null
        override suspend fun getQuizWithDetails(quizId: Long): QuizWithQuestionsAndMaterials? = quizToReturn
        override fun getAllQuizzesWithDetails(): Flow<List<QuizWithQuestionsAndMaterials>> = emptyFlow()
        override suspend fun getQuestionsForQuiz(quizId: Long, questionType: String?): List<QuestionEntity> = emptyList()
        override suspend fun deleteQuizById(quizId: Long) {}
    }
}
