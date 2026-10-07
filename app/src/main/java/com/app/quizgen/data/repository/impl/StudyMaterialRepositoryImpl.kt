package com.app.quizgen.data.repository.impl

import com.app.quizgen.data.local.dao.StudyMaterialDao
import com.app.quizgen.data.local.entity.StudyMaterialEntity
import com.app.quizgen.data.repository.StudyMaterialRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [StudyMaterialRepository] backed by Room DAO.
 *
 * Injected as a singleton via Hilt. All suspend functions run on
 * the caller's coroutine context (typically Dispatchers.IO from ViewModel).
 */
@Singleton
class StudyMaterialRepositoryImpl @Inject constructor(
    private val studyMaterialDao: StudyMaterialDao
) : StudyMaterialRepository {

    override suspend fun insertMaterial(material: StudyMaterialEntity): Long {
        return studyMaterialDao.insert(material)
    }

    override fun getAllMaterials(): Flow<List<StudyMaterialEntity>> {
        return studyMaterialDao.getAllMaterials()
    }

    override suspend fun getMaterialById(materialId: Long): StudyMaterialEntity? {
        return studyMaterialDao.getMaterialById(materialId)
    }

    override suspend fun getMaterialsByIds(materialIds: List<Long>): List<StudyMaterialEntity> {
        return studyMaterialDao.getMaterialsByIds(materialIds)
    }

    override fun getMaterialsByFileType(fileType: String): Flow<List<StudyMaterialEntity>> {
        return studyMaterialDao.getMaterialsByFileType(fileType)
    }

    override suspend fun deleteMaterial(material: StudyMaterialEntity) {
        studyMaterialDao.delete(material)
    }

    override suspend fun deleteMaterialById(materialId: Long) {
        studyMaterialDao.deleteById(materialId)
    }
}
