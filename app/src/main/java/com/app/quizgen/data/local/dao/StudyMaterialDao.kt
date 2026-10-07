package com.app.quizgen.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.app.quizgen.data.local.entity.StudyMaterialEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for StudyMaterial operations.
 *
 * Provides CRUD operations for the study_materials table.
 */
@Dao
interface StudyMaterialDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(material: StudyMaterialEntity): Long

    @Query("SELECT * FROM study_materials ORDER BY uploaded_at DESC")
    fun getAllMaterials(): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE material_id = :materialId")
    suspend fun getMaterialById(materialId: Long): StudyMaterialEntity?

    @Query("SELECT * FROM study_materials WHERE material_id IN (:materialIds)")
    suspend fun getMaterialsByIds(materialIds: List<Long>): List<StudyMaterialEntity>

    @Query("SELECT * FROM study_materials WHERE file_type = :fileType ORDER BY uploaded_at DESC")
    fun getMaterialsByFileType(fileType: String): Flow<List<StudyMaterialEntity>>

    @Delete
    suspend fun delete(material: StudyMaterialEntity)

    @Query("DELETE FROM study_materials WHERE material_id = :materialId")
    suspend fun deleteById(materialId: Long)
}
