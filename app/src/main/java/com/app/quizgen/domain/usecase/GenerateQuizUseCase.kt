package com.app.quizgen.domain.usecase

import com.app.quizgen.data.local.entity.QuizEntity
import com.app.quizgen.data.remote.GeminiApiException
import com.app.quizgen.data.remote.GeminiClient
import com.app.quizgen.data.remote.GeminiResponseParser
import com.app.quizgen.data.remote.ParsedQuizResult
import com.app.quizgen.data.remote.PromptBuilder
import com.app.quizgen.data.remote.QuizParsingException
import com.app.quizgen.data.repository.QuizRepository
import com.app.quizgen.data.repository.StudyMaterialRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parameters for quiz generation.
 */
data class GenerateQuizParams(
    val materialIds: List<Long>,
    val difficulty: String, // "EASY", "MEDIUM", "HARD"
    val mcqCount: Int,
    val shortQCount: Int,
    val longQCount: Int,
    val customTitle: String? = null
)

/**
 * Orchestrates end-to-end quiz generation:
 * 1. Validates inputs (materials, counts, difficulty).
 * 2. Fetches study materials and combines their extracted text.
 * 3. Constructs the dynamic AI prompt via [PromptBuilder].
 * 4. Calls Gemini AI via [GeminiClient] with retry logic for transient or parsing errors.
 * 5. Parses and normalizes the response via [GeminiResponseParser].
 * 6. Atomically persists the Quiz, Questions, and CrossRefs via [QuizRepository].
 *
 * Implements Task 4.4 from dev_work_phases.md.
 */
@Singleton
class GenerateQuizUseCase @Inject constructor(
    private val studyMaterialRepository: StudyMaterialRepository,
    private val quizRepository: QuizRepository,
    private val geminiClient: GeminiClient,
    private val promptBuilder: PromptBuilder,
    private val responseParser: GeminiResponseParser
) {

    companion object {
        private const val MAX_RETRIES = 2
        private const val RETRY_DELAY_MS = 1000L
    }

    /**
     * Executes quiz generation with the given parameters.
     *
     * @param params Generation parameters.
     * @return [Result] containing the generated quiz ID on success, or exception on failure.
     */
    suspend operator fun invoke(params: GenerateQuizParams): Result<Long> {
        return invoke(
            materialIds = params.materialIds,
            difficulty = params.difficulty,
            mcqCount = params.mcqCount,
            shortQCount = params.shortQCount,
            longQCount = params.longQCount,
            customTitle = params.customTitle
        )
    }

    /**
     * Executes quiz generation with individual parameters.
     *
     * @param materialIds List of source study material IDs.
     * @param difficulty "EASY", "MEDIUM", or "HARD".
     * @param mcqCount Number of MCQs.
     * @param shortQCount Number of Short questions.
     * @param longQCount Number of Long questions.
     * @param customTitle Optional title override.
     * @return [Result] containing the generated quiz ID on success.
     */
    suspend operator fun invoke(
        materialIds: List<Long>,
        difficulty: String,
        mcqCount: Int,
        shortQCount: Int,
        longQCount: Int,
        customTitle: String? = null
    ): Result<Long> {
        return runCatching {
            // 1. Validate inputs
            if (materialIds.isEmpty()) {
                throw IllegalArgumentException("Please select at least one study material.")
            }
            if (mcqCount < 0 || shortQCount < 0 || longQCount < 0) {
                throw IllegalArgumentException("Question counts cannot be negative.")
            }
            val totalRequested = mcqCount + shortQCount + longQCount
            if (totalRequested <= 0) {
                throw IllegalArgumentException("At least one question must be requested (total count must be > 0).")
            }

            val normalizedDifficulty = when (difficulty.trim().uppercase()) {
                "EASY" -> "EASY"
                "HARD" -> "HARD"
                else -> "MEDIUM"
            }

            // 2. Fetch study materials
            val materials = studyMaterialRepository.getMaterialsByIds(materialIds)
            if (materials.isEmpty()) {
                throw IllegalStateException("No study materials found for the selected IDs: $materialIds")
            }

            val hasText = materials.any { it.extractedText.isNotBlank() }
            if (!hasText) {
                throw IllegalStateException("Selected study materials contain no extractable text content.")
            }

            // 3. Build dynamic prompt
            val prompt = promptBuilder.buildQuizPrompt(
                materials = materials,
                difficulty = normalizedDifficulty,
                mcqCount = mcqCount,
                shortQCount = shortQCount,
                longQCount = longQCount
            )

            // 4. Call Gemini AI with retry handling
            val parsedQuiz = executeAiGenerationWithRetry(prompt)

            // 5. Build QuizEntity metadata
            val finalTitle = customTitle?.takeIf { it.isNotBlank() }
                ?: parsedQuiz.title.takeIf { it.isNotBlank() }
                ?: "Quiz - ${materials.first().title}"

            val actualMcqCount = parsedQuiz.questions.count { it.questionType == "MCQ" }
            val actualShortCount = parsedQuiz.questions.count { it.questionType == "SHORT" }
            val actualLongCount = parsedQuiz.questions.count { it.questionType == "LONG" }

            val quizEntity = QuizEntity(
                quizId = 0,
                title = finalTitle,
                difficultyLevel = normalizedDifficulty,
                totalQuestions = parsedQuiz.questions.size,
                mcqCount = actualMcqCount,
                shortQCount = actualShortCount,
                longQCount = actualLongCount,
                createdAt = System.currentTimeMillis()
            )

            // 6. Save atomically in Room DB (Quiz + Questions + CrossRefs)
            val generatedQuizId = quizRepository.createQuizWithQuestionsAndMaterials(
                quiz = quizEntity,
                questions = parsedQuiz.questions,
                materialIds = materialIds
            )

            generatedQuizId
        }
    }

    /**
     * Executes the Gemini call and JSON parsing, retrying on transient
     * API errors or malformed JSON payloads.
     */
    private suspend fun executeAiGenerationWithRetry(prompt: String): ParsedQuizResult {
        var lastException: Exception? = null

        for (attempt in 1..MAX_RETRIES) {
            try {
                val rawResponse = geminiClient.generateContent(prompt)
                return responseParser.parse(rawResponse)
            } catch (e: QuizParsingException) {
                lastException = e
                if (attempt < MAX_RETRIES) {
                    delay(RETRY_DELAY_MS * attempt)
                }
            } catch (e: GeminiApiException) {
                lastException = e
                if (attempt < MAX_RETRIES) {
                    delay(RETRY_DELAY_MS * attempt)
                }
            }
        }

        throw lastException ?: IllegalStateException("Quiz generation failed after $MAX_RETRIES attempts.")
    }
}
