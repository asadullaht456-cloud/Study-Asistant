package com.app.quizgen.data.repository

import com.app.quizgen.data.local.entity.StudyMaterialEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstraction for StudyMaterial operations.
 *
 * Exposes suspend functions for one-shot operations and
 * Flow-based queries for reactive UI observation.
 */
interface StudyMaterialRepository {

    /** Insert a new study material, returns the generated material_id. */
    suspend fun insertMaterial(material: StudyMaterialEntity): Long

    /** Observe all materials ordered by upload date (newest first). */
    fun getAllMaterials(): Flow<List<StudyMaterialEntity>>

    /** Fetch a single material by ID (one-shot). */
    suspend fun getMaterialById(materialId: Long): StudyMaterialEntity?

    /** Fetch multiple materials by their IDs (for quiz generation batch). */
    suspend fun getMaterialsByIds(materialIds: List<Long>): List<StudyMaterialEntity>

    /** Observe materials filtered by file type (PDF/DOCX/TXT). */
    fun getMaterialsByFileType(fileType: String): Flow<List<StudyMaterialEntity>>

    /** Delete a material by its entity. */
    suspend fun deleteMaterial(material: StudyMaterialEntity)

    /** Delete a material by ID. */
    suspend fun deleteMaterialById(materialId: Long)
}
