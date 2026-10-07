package com.app.quizgen.data.repository.impl

import androidx.room.withTransaction
import com.app.quizgen.data.local.AppDatabase
import com.app.quizgen.data.local.dao.QuestionDao
import com.app.quizgen.data.local.dao.QuizDao
import com.app.quizgen.data.local.dao.QuizMaterialCrossRefDao
import com.app.quizgen.data.local.entity.QuestionEntity
import com.app.quizgen.data.local.entity.QuizEntity
import com.app.quizgen.data.local.entity.QuizMaterialCrossRef
import com.app.quizgen.data.local.relation.QuizWithQuestionsAndMaterials
import com.app.quizgen.data.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [QuizRepository] backed by Room DAOs.
 *
 * The critical method [createQuizWithQuestionsAndMaterials] uses Room's
 * withTransaction to ensure atomicity: if any insert fails, the entire
 * quiz creation (quiz + questions + cross-refs) is rolled back.
 */
@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val quizDao: QuizDao,
    private val questionDao: QuestionDao,
    private val crossRefDao: QuizMaterialCrossRefDao
) : QuizRepository {

    /**
     * Atomically creates a quiz with all its questions and material cross-references.
     *
     * Uses Room's [withTransaction] for full atomicity — if any step fails,
     * all inserts are rolled back.
     *
     * Transaction flow:
     * 1. Insert QuizEntity → retrieve auto-generated quiz_id
     * 2. Map each QuestionEntity to use the new quiz_id, then batch insert
     * 3. Create QuizMaterialCrossRef entries linking quiz_id to each material_id
     */
    override suspend fun createQuizWithQuestionsAndMaterials(
        quiz: QuizEntity,
        questions: List<QuestionEntity>,
        materialIds: List<Long>
    ): Long {
        return appDatabase.withTransaction {
            // Step 1: Insert quiz and get auto-generated ID
            val generatedQuizId = quizDao.insert(quiz)

            // Step 2: Link questions to the generated quiz ID and batch insert
            val linkedQuestions = questions.map { question ->
                question.copy(quizId = generatedQuizId)
            }
            questionDao.insertAll(linkedQuestions)

            // Step 3: Create cross-reference entries for source materials
            val crossRefs = materialIds.map { materialId ->
                QuizMaterialCrossRef(
                    quizId = generatedQuizId,
                    materialId = materialId
                )
            }
            crossRefDao.insertAll(crossRefs)

            generatedQuizId
        }
    }

    override fun getAllQuizzes(): Flow<List<QuizEntity>> {
        return quizDao.getAllQuizzes()
    }

    override suspend fun getQuizById(quizId: Long): QuizEntity? {
        return quizDao.getQuizById(quizId)
    }

    override suspend fun getQuizWithDetails(quizId: Long): QuizWithQuestionsAndMaterials? {
        return quizDao.getQuizWithQuestionsAndMaterials(quizId)
    }

    override fun getAllQuizzesWithDetails(): Flow<List<QuizWithQuestionsAndMaterials>> {
        return quizDao.getAllQuizzesWithDetails()
    }

    override suspend fun getQuestionsForQuiz(quizId: Long, questionType: String?): List<QuestionEntity> {
        return if (questionType != null) {
            questionDao.getQuestionsByQuizIdAndType(quizId, questionType)
        } else {
            questionDao.getQuestionsByQuizId(quizId)
        }
    }

    override suspend fun deleteQuizById(quizId: Long) {
        // CASCADE foreign keys will automatically delete associated
        // questions and quiz_material_cross_ref entries
        quizDao.deleteById(quizId)
    }
}
