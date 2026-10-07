package com.app.quizgen.domain.usecase

import com.app.quizgen.data.local.entity.QuestionEntity
import com.app.quizgen.data.local.entity.QuizEntity
import com.app.quizgen.data.local.entity.StudyMaterialEntity
import com.app.quizgen.data.local.relation.QuizWithQuestionsAndMaterials
import com.app.quizgen.data.remote.GeminiApiException
import com.app.quizgen.data.remote.GeminiClient
import com.app.quizgen.data.remote.GeminiResponseParser
import com.app.quizgen.data.remote.PromptBuilder
import com.app.quizgen.data.remote.QuizParsingException
import com.app.quizgen.data.repository.QuizRepository
import com.app.quizgen.data.repository.StudyMaterialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateQuizUseCaseTest {

    private lateinit var fakeStudyMaterialRepo: FakeStudyMaterialRepository
    private lateinit var fakeQuizRepo: FakeQuizRepository
    private lateinit var fakeGeminiClient: FakeGeminiClient
    private lateinit var promptBuilder: PromptBuilder
    private lateinit var responseParser: GeminiResponseParser
    private lateinit var useCase: GenerateQuizUseCase

    @Before
    fun setUp() {
        fakeStudyMaterialRepo = FakeStudyMaterialRepository()
        fakeQuizRepo = FakeQuizRepository()
        fakeGeminiClient = FakeGeminiClient()
        promptBuilder = PromptBuilder()
        responseParser = GeminiResponseParser()

        useCase = GenerateQuizUseCase(
            studyMaterialRepository = fakeStudyMaterialRepo,
            quizRepository = fakeQuizRepo,
            geminiClient = fakeGeminiClient,
            promptBuilder = promptBuilder,
            responseParser = responseParser
        )
    }

    @Test
    fun `invoke fails when materialIds is empty`() = runBlocking {
        val result = useCase(
            materialIds = emptyList(),
            difficulty = "MEDIUM",
            mcqCount = 5,
            shortQCount = 2,
            longQCount = 1
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()?.message?.contains("at least one study material") == true)
    }

    @Test
    fun `invoke fails when total question count is zero`() = runBlocking {
        val result = useCase(
            materialIds = listOf(1L),
            difficulty = "MEDIUM",
            mcqCount = 0,
            shortQCount = 0,
            longQCount = 0
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()?.message?.contains("greater than zero") == true)
    }

    @Test
    fun `invoke fails when question counts are negative`() = runBlocking {
        val result = useCase(
            materialIds = listOf(1L),
            difficulty = "MEDIUM",
            mcqCount = -1,
            shortQCount = 0,
            longQCount = 0
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(result.exceptionOrNull()?.message?.contains("negative") == true)
    }

    @Test
    fun `invoke fails when study material has empty extracted text`() = runBlocking {
        fakeStudyMaterialRepo.addMaterial(
            StudyMaterialEntity(
                material_id = 1L,
                title = "EmptyDoc.pdf",
                file_type = "PDF",
                file_uri = "file:///empty.pdf",
                extracted_text = "   ",
                file_size_mb = 1.0,
                uploaded_at = 1000L
            )
        )

        val result = useCase(
            materialIds = listOf(1L),
            difficulty = "MEDIUM",
            mcqCount = 5,
            shortQCount = 0,
            longQCount = 0
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertTrue(result.exceptionOrNull()?.message?.contains("no extractable text") == true)
    }

    @Test
    fun `invoke successfully completes full pipeline and saves quiz`() = runBlocking {
        fakeStudyMaterialRepo.addMaterial(
            StudyMaterialEntity(
                material_id = 10L,
                title = "Operating_Systems.txt",
                file_type = "TXT",
                file_uri = "file:///os.txt",
                extracted_text = "Virtual memory uses paging to map virtual addresses to physical frames.",
                file_size_mb = 1.2,
                uploaded_at = 2000L
            )
        )

        fakeGeminiClient.responseToReturn = """
        {
          "title": "Virtual Memory Quiz",
          "questions": [
            {
              "question_type": "MCQ",
              "question_text": "What structure maps virtual addresses to physical pages?",
              "options": ["Page Table", "Segment Tree", "Registry", "Heap"],
              "correct_answer": "Page Table",
              "explanation": "Page tables store the mapping between virtual and physical frames."
            },
            {
              "question_type": "SHORT",
              "question_text": "What is a page fault?",
              "options": null,
              "correct_answer": "An interrupt raised when a program accesses a memory page not currently mapped in RAM.",
              "explanation": "Must state it is a hardware trap / interrupt."
            }
          ]
        }
        """.trimIndent()

        val result = useCase(
            materialIds = listOf(10L),
            difficulty = "HARD",
            mcqCount = 1,
            shortQCount = 1,
            longQCount = 0
        )

        assertTrue(result.isSuccess)
        val generatedQuizId = result.getOrThrow()
        assertEquals(101L, generatedQuizId)

        // Verify quiz was persisted in repository
        val createdQuiz = fakeQuizRepo.savedQuiz
        assertEquals("Virtual Memory Quiz", createdQuiz?.title)
        assertEquals("HARD", createdQuiz?.difficultyLevel)
        assertEquals(2, createdQuiz?.totalQuestions)
        assertEquals(1, createdQuiz?.mcqCount)
        assertEquals(1, createdQuiz?.shortQCount)
        assertEquals(2, fakeQuizRepo.savedQuestions.size)
        assertEquals(listOf(10L), fakeQuizRepo.savedMaterialIds)
    }

    // --- Fake Test Implementations ---

    private class FakeGeminiClient : GeminiClient() {
        var responseToReturn: String = ""

        override suspend fun generateContent(prompt: String): String {
            if (responseToReturn.isBlank()) {
                throw GeminiApiException("No fake response configured")
            }
            return responseToReturn
        }
    }

    private class FakeStudyMaterialRepository : StudyMaterialRepository {
        private val materials = mutableMapOf<Long, StudyMaterialEntity>()

        fun addMaterial(entity: StudyMaterialEntity) {
            materials[entity.material_id] = entity
        }

        override suspend fun insertMaterial(material: StudyMaterialEntity): Long = 1L
        override fun getAllMaterials(): Flow<List<StudyMaterialEntity>> = emptyFlow()
        override suspend fun getMaterialById(materialId: Long): StudyMaterialEntity? = materials[materialId]
        override suspend fun getMaterialsByIds(materialIds: List<Long>): List<StudyMaterialEntity> {
            return materialIds.mapNotNull { materials[it] }
        }
        override fun getMaterialsByFileType(fileType: String): Flow<List<StudyMaterialEntity>> = emptyFlow()
        override suspend fun deleteMaterial(material: StudyMaterialEntity) {}
        override suspend fun deleteMaterialById(materialId: Long) {}
    }

    private class FakeQuizRepository : QuizRepository {
        var savedQuiz: QuizEntity? = null
        var savedQuestions: List<QuestionEntity> = emptyList()
        var savedMaterialIds: List<Long> = emptyList()

        override suspend fun createQuizWithQuestionsAndMaterials(
            quiz: QuizEntity,
            questions: List<QuestionEntity>,
            materialIds: List<Long>
        ): Long {
            savedQuiz = quiz
            savedQuestions = questions
            savedMaterialIds = materialIds
            return 101L
        }

        override fun getAllQuizzes(): Flow<List<QuizEntity>> = emptyFlow()
        override suspend fun getQuizById(quizId: Long): QuizEntity? = null
        override suspend fun getQuizWithDetails(quizId: Long): QuizWithQuestionsAndMaterials? = null
        override fun getAllQuizzesWithDetails(): Flow<List<QuizWithQuestionsAndMaterials>> = emptyFlow()
        override suspend fun getQuestionsForQuiz(quizId: Long, questionType: String?): List<QuestionEntity> = emptyList()
        override suspend fun deleteQuizById(quizId: Long) {}
    }
}
