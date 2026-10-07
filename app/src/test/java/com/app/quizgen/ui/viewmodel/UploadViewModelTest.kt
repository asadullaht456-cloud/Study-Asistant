package com.app.quizgen.ui.viewmodel

import com.app.quizgen.data.local.entity.StudyMaterialEntity
import com.app.quizgen.data.parser.FileParserFactory
import com.app.quizgen.data.storage.FileStorageManager
import com.app.quizgen.data.repository.StudyMaterialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UploadViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeStorageManager: FakeStorageManager
    private lateinit var fakeParserFactory: FakeParserFactory
    private lateinit var fakeMaterialRepo: FakeStudyMaterialRepository
    private lateinit var viewModel: UploadViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeStorageManager = FakeStorageManager()
        fakeParserFactory = FakeParserFactory()
        fakeMaterialRepo = FakeStudyMaterialRepository()

        viewModel = UploadViewModel(
            fileStorageManager = fakeStorageManager,
            fileParserFactory = fakeParserFactory,
            studyMaterialRepository = fakeMaterialRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle and selected list is empty`() {
        assertEquals(UploadUiState.Idle, viewModel.uploadState.value)
        assertTrue(viewModel.selectedMaterialIds.value.isEmpty())
    }

    @Test
    fun `toggleMaterialSelection toggles id in selection set`() {
        viewModel.toggleMaterialSelection(10L)
        assertTrue(viewModel.selectedMaterialIds.value.contains(10L))

        viewModel.toggleMaterialSelection(20L)
        assertEquals(2, viewModel.selectedMaterialIds.value.size)

        viewModel.toggleMaterialSelection(10L)
        assertFalse(viewModel.selectedMaterialIds.value.contains(10L))
        assertTrue(viewModel.selectedMaterialIds.value.contains(20L))
    }

    @Test
    fun `clearSelection empties the selection set`() {
        viewModel.toggleMaterialSelection(1L)
        viewModel.toggleMaterialSelection(2L)
        assertEquals(2, viewModel.selectedMaterialIds.value.size)

        viewModel.clearSelection()
        assertTrue(viewModel.selectedMaterialIds.value.isEmpty())
    }

    @Test
    fun `resetUploadState sets state to Idle`() {
        viewModel.resetUploadState()
        assertEquals(UploadUiState.Idle, viewModel.uploadState.value)
    }

    // --- Fakes ---

    private class FakeStorageManager : FileStorageManager(null)

    private class FakeParserFactory : FileParserFactory()

    private class FakeStudyMaterialRepository : StudyMaterialRepository {
        private val list = mutableListOf<StudyMaterialEntity>()
        private val flow = MutableStateFlow<List<StudyMaterialEntity>>(emptyList())

        override suspend fun insertMaterial(material: StudyMaterialEntity): Long {
            list.add(material)
            flow.value = list
            return 1L
        }

        override fun getAllMaterials(): Flow<List<StudyMaterialEntity>> = flow
        override suspend fun getMaterialById(materialId: Long): StudyMaterialEntity? = null
        override suspend fun getMaterialsByIds(materialIds: List<Long>): List<StudyMaterialEntity> = emptyList()
        override fun getMaterialsByFileType(fileType: String): Flow<List<StudyMaterialEntity>> = MutableStateFlow(emptyList())
        override suspend fun deleteMaterial(material: StudyMaterialEntity) {}
        override suspend fun deleteMaterialById(materialId: Long) {}
    }
}
