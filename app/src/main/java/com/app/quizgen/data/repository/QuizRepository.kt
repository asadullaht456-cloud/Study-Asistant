package com.app.quizgen.data.repository

import com.app.quizgen.data.local.entity.QuestionEntity
import com.app.quizgen.data.local.entity.QuizEntity
import com.app.quizgen.data.local.relation.QuizWithQuestionsAndMaterials
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstraction for Quiz and Question operations.
 *
 * Handles quiz creation (including questions and cross-refs in a
 * single transaction), retrieval, and deletion with cascading.
 */
interface QuizRepository {

    /**
     * Create a complete quiz atomically:
     * 1. Insert QuizEntity → get quiz_id
     * 2. Insert all QuestionEntities linked to quiz_id
     * 3. Insert QuizMaterialCrossRef entries for source materials
     *
     * @param quiz The quiz metadata entity (quizId will be auto-generated)
     * @param questions List of questions to insert (quizId field will be overwritten)
     * @param materialIds IDs of the source study materials used
     * @return The generated quiz_id
     */
    suspend fun createQuizWithQuestionsAndMaterials(
        quiz: QuizEntity,
        questions: List<QuestionEntity>,
        materialIds: List<Long>
    ): Long

    /** Observe all quizzes ordered by creation date (newest first). */
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    /** Fetch a single quiz by ID (one-shot). */
    suspend fun getQuizById(quizId: Long): QuizEntity?

    /** Fetch a quiz with all its questions and source materials (one-shot). */
    suspend fun getQuizWithDetails(quizId: Long): QuizWithQuestionsAndMaterials?

    /** Observe all quizzes with full relational data. */
    fun getAllQuizzesWithDetails(): Flow<List<QuizWithQuestionsAndMaterials>>

    /** Fetch questions for a quiz, optionally filtered by type. */
    suspend fun getQuestionsForQuiz(quizId: Long, questionType: String? = null): List<QuestionEntity>

    /** Delete a quiz by ID (cascades to questions and cross-refs). */
    suspend fun deleteQuizById(quizId: Long)
}
