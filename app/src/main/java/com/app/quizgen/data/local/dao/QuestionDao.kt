package com.app.quizgen.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.quizgen.data.local.entity.QuestionEntity

/**
 * Data Access Object for Question operations.
 *
 * Supports batch insert (for AI-generated question sets) and
 * filtered queries by quiz ID and question type.
 */
@Dao
interface QuestionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: QuestionEntity): Long

    @Query("SELECT * FROM questions WHERE quiz_id = :quizId ORDER BY question_id ASC")
    suspend fun getQuestionsByQuizId(quizId: Long): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE quiz_id = :quizId AND question_type = :type ORDER BY question_id ASC")
    suspend fun getQuestionsByQuizIdAndType(quizId: Long, type: String): List<QuestionEntity>

    @Query("DELETE FROM questions WHERE quiz_id = :quizId")
    suspend fun deleteByQuizId(quizId: Long)
}
