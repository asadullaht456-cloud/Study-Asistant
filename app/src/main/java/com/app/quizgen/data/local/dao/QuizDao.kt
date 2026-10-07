package com.app.quizgen.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.app.quizgen.data.local.entity.QuizEntity
import com.app.quizgen.data.local.relation.QuizWithQuestionsAndMaterials
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Quiz operations.
 *
 * Provides insert, query, and delete for the quizzes table,
 * plus transactional loading of full quiz data with questions and materials.
 */
@Dao
interface QuizDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quiz: QuizEntity): Long

    @Query("SELECT * FROM quizzes ORDER BY created_at DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE quiz_id = :quizId")
    suspend fun getQuizById(quizId: Long): QuizEntity?

    @Transaction
    @Query("SELECT * FROM quizzes WHERE quiz_id = :quizId")
    suspend fun getQuizWithQuestionsAndMaterials(quizId: Long): QuizWithQuestionsAndMaterials?

    @Transaction
    @Query("SELECT * FROM quizzes ORDER BY created_at DESC")
    fun getAllQuizzesWithDetails(): Flow<List<QuizWithQuestionsAndMaterials>>

    @Query("DELETE FROM quizzes WHERE quiz_id = :quizId")
    suspend fun deleteById(quizId: Long)
}
