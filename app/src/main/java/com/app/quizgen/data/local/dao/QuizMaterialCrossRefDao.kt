package com.app.quizgen.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.quizgen.data.local.entity.QuizMaterialCrossRef
import com.app.quizgen.data.local.entity.StudyMaterialEntity

/**
 * Data Access Object for the Quiz ↔ StudyMaterial junction table.
 *
 * Manages the many-to-many relationship between quizzes and
 * the study materials used to generate them.
 */
@Dao
interface QuizMaterialCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(crossRef: QuizMaterialCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(crossRefs: List<QuizMaterialCrossRef>)

    @Query("""
        SELECT sm.* FROM study_materials sm
        INNER JOIN quiz_material_cross_ref qmcr ON sm.material_id = qmcr.material_id
        WHERE qmcr.quiz_id = :quizId
    """)
    suspend fun getMaterialsForQuiz(quizId: Long): List<StudyMaterialEntity>

    @Query("DELETE FROM quiz_material_cross_ref WHERE quiz_id = :quizId")
    suspend fun deleteByQuizId(quizId: Long)
}
