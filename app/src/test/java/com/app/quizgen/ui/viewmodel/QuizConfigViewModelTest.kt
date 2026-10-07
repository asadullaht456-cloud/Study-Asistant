package com.app.quizgen.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.app.quizgen.data.local.entity.QuestionEntity
import com.app.quizgen.data.local.entity.QuizEntity
import com.app.quizgen.data.local.entity.StudyMaterialEntity
import com.app.quizgen.data.local.relation.QuizWithQuestionsAndMaterials
import com.app.quizgen.data.remote.GeminiClient
import com.app.quizgen.data.remote.GeminiResponseParser
import com.app.quizgen.data.remote.PromptBuilder
import com.app.quizgen.data.repository.QuizRepository
import com.app.quizgen.data.repository.StudyMaterialRepository
import com.app.quizgen.domain.usecase.GenerateQuizUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizConfigViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeMaterialRepo: FakeStudyMaterialRepository
    private lateinit var fakeQuizRepo: FakeQuizRepository
    private lateinit var fakeGeminiClient: FakeGeminiClient
    private lateinit var useCase: GenerateQuizUseCase
    private lateinit var viewModel: QuizConfigViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeMaterialRepo = FakeStudyMaterialRepository()
        fakeQuizRepo = FakeQuizRepository()
        fakeGeminiClient = FakeGeminiClient()

        useCase = GenerateQuizUseCase(
            studyMaterialRepository = fakeMaterialRepo,
            quizRepository = fakeQuizRepo,
            geminiClient = fakeGeminiClient,
            promptBuilder = PromptBuilder(),
            responseParser = GeminiResponseParser()
        )

        viewModel = QuizConfigViewModel(
            studyMaterialRepository = fakeMaterialRepo,
            generateQuizUseCase = useCase,
            savedStateHandle = SavedStateHandle()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default values match requirements`() {
        assertEquals("MEDIUM", viewModel.difficulty.value)
        assertEquals(10, viewModel.mcqCount.value)
        assertEquals(3, viewModel.shortQCount.value)
        assertEquals(1, viewModel.longQCount.value)
        assertEquals(14, viewModel.totalQuestions.value)
    }

    @Test
    fun `counter increments and decrements work correctly without going negative`() {
        viewModel.decrementLongQ()
        assertEquals(0, viewModel.longQCount.value)

        // Decrement below 0 should stay at 0
        viewModel.decrementLongQ()
        assertEquals(0, viewModel.longQCount.value)

        viewModel.incrementLongQ()
        assertEquals(1, viewModel.longQCount.value)
    }

    @Test
    fun `setDifficulty updates only valid difficulty values`() {
        viewModel.setDifficulty("HARD")
        assertEquals("HARD", viewModel.difficulty.value)

        viewModel.setDifficulty("EASY")
        assertEquals("EASY", viewModel.difficulty.value)

        viewModel.setDifficulty("INVALID")
        assertEquals("EASY", viewModel.difficulty.value)
    }

    @Test
    fun `removeMaterial removes item from selected list`() {
        val mat1 = createSampleMaterial(1L, "File1.pdf")
        val mat2 = createSampleMaterial(2L, "File2.docx")
        viewModel.setSelectedMaterials(listOf(mat1, mat2))

        assertEquals(2, viewModel.selectedMaterials.value.size)

        viewModel.removeMaterial(1L)
        assertEquals(1, viewModel.selectedMaterials.value.size)
        assertEquals(2L, viewModel.selectedMaterials.value.first().material_id)
    }

    @Test
    fun `generateQuiz with empty materials produces error state`() = runTest {
        viewModel.setSelectedMaterials(emptyList())
        viewModel.generateQuiz()

        assertTrue(viewModel.generationState.value is QuizGenerationUiState.Error)
    }

    @Test
    fun `generateQuiz successful execution transitions through states`() = runTest {
        val mat = createSampleMaterial(5L, "Sample.txt", "Some relevant content for quiz generation.")
        fakeMaterialRepo.add(mat)
        viewModel.setSelectedMaterials(listOf(mat))

        fakeGeminiClient.response = """
        {
          "title": "Config Generated Quiz",
          "questions": [
            {
              "question_type": "MCQ",
              "question_text": "Q1?",
              "options": ["A", "B", "C", "D"],
              "correct_answer": "A",
              "explanation": "Because A."
            }
          ]
        }
        """.trimIndent()

        viewModel.setMcqCount(1)
        viewModel.setShortQCount(0)
        viewModel.setLongQCount(0)

        viewModel.generateQuiz()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.generationState.value is QuizGenerationUiState.Success)
        val successState = viewModel.generationState.value as QuizGenerationUiState.Success
        assertEquals(100L, successState.quizId)
    }

    private fun createSampleMaterial(
        id: Long,
        title: String,
        text: String = "Sample content"
    ) = StudyMaterialEntity(
        material_id = id,
        title = title,
        file_type = "TXT",
        file_uri = "file:///sample.txt",
        extracted_text = text,
        file_size_mb = 1.0,
        uploaded_at = 1000L
    )

    private class FakeGeminiClient : GeminiClient() {
        var response = ""
        override suspend fun generateContent(prompt: String): String = response
    }

    private class FakeStudyMaterialRepository : StudyMaterialRepository {
        private val map = mutableMapOf<Long, StudyMaterialEntity>()
        fun add(e: StudyMaterialEntity) { map[e.material_id] = e }
        override suspend fun insertMaterial(material: StudyMaterialEntity): Long = 1L
        override fun getAllMaterials(): Flow<List<StudyMaterialEntity>> = emptyFlow()
        override suspend fun getMaterialById(materialId: Long): StudyMaterialEntity? = map[materialId]
        override suspend fun getMaterialsByIds(materialIds: List<Long>): List<StudyMaterialEntity> =
            materialIds.mapNotNull { map[it] }
        override fun getMaterialsByFileType(fileType: String): Flow<List<StudyMaterialEntity>> = emptyFlow()
        override suspend fun deleteMaterial(material: StudyMaterialEntity) {}
        override suspend fun deleteMaterialById(materialId: Long) {}
    }

    private class FakeQuizRepository : QuizRepository {
        override suspend fun createQuizWithQuestionsAndMaterials(
            quiz: QuizEntity,
            questions: List<QuestionEntity>,
            materialIds: List<Long>
        ): Long = 100L

        override fun getAllQuizzes(): Flow<List<QuizEntity>> = emptyFlow()
        override suspend fun getQuizById(quizId: Long): QuizEntity? = null
        override suspend fun getQuizWithDetails(quizId: Long): QuizWithQuestionsAndMaterials? = null
        override fun getAllQuizzesWithDetails(): Flow<List<QuizWithQuestionsAndMaterials>> = emptyFlow()
        override suspend fun getQuestionsForQuiz(quizId: Long, questionType: String?): List<QuestionEntity> = emptyList()
        override suspend fun deleteQuizById(quizId: Long) {}
    }
}
