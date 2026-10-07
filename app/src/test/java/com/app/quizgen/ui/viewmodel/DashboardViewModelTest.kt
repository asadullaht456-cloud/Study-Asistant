package com.app.quizgen.ui.viewmodel

import com.app.quizgen.data.local.entity.QuestionEntity
import com.app.quizgen.data.local.entity.QuizEntity
import com.app.quizgen.data.local.entity.StudyMaterialEntity
import com.app.quizgen.data.local.relation.QuizWithQuestionsAndMaterials
import com.app.quizgen.data.repository.QuizRepository
import com.app.quizgen.data.repository.StudyMaterialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeQuizRepo: FakeQuizRepository
    private lateinit var fakeMaterialRepo: FakeStudyMaterialRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeQuizRepo = FakeQuizRepository()
        fakeMaterialRepo = FakeStudyMaterialRepository()
        viewModel = DashboardViewModel(fakeQuizRepo, fakeMaterialRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `default tab is GENERATED_QUIZZES`() {
        assertEquals(DashboardTab.GENERATED_QUIZZES, viewModel.selectedTab.value)
    }

    @Test
    fun `selectTab updates selectedTab value`() {
        viewModel.selectTab(DashboardTab.SAVED_MATERIALS)
        assertEquals(DashboardTab.SAVED_MATERIALS, viewModel.selectedTab.value)

        viewModel.selectTab(DashboardTab.GENERATED_QUIZZES)
        assertEquals(DashboardTab.GENERATED_QUIZZES, viewModel.selectedTab.value)
    }

    @Test
    fun `deleteQuiz invokes repository delete`() = runTest {
        viewModel.deleteQuiz(42L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(42L, fakeQuizRepo.lastDeletedQuizId)
        assertEquals("Quiz deleted successfully.", viewModel.userMessage.value)
    }

    @Test
    fun `deleteMaterial invokes repository delete`() = runTest {
        viewModel.deleteMaterial(99L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(99L, fakeMaterialRepo.lastDeletedMaterialId)
        assertEquals("Material deleted successfully.", viewModel.userMessage.value)
    }

    // --- Fakes ---

    private class FakeQuizRepository : QuizRepository {
        var lastDeletedQuizId: Long? = null
        private val quizzesFlow = MutableStateFlow<List<QuizEntity>>(emptyList())

        override suspend fun createQuizWithQuestionsAndMaterials(
            quiz: QuizEntity,
            questions: List<QuestionEntity>,
            materialIds: List<Long>
        ): Long = 1L

        override fun getAllQuizzes(): Flow<List<QuizEntity>> = quizzesFlow
        override suspend fun getQuizById(quizId: Long): QuizEntity? = null
        override suspend fun getQuizWithDetails(quizId: Long): QuizWithQuestionsAndMaterials? = null
        override fun getAllQuizzesWithDetails(): Flow<List<QuizWithQuestionsAndMaterials>> = MutableStateFlow(emptyList())
        override suspend fun getQuestionsForQuiz(quizId: Long, questionType: String?): List<QuestionEntity> = emptyList()

        override suspend fun deleteQuizById(quizId: Long) {
            lastDeletedQuizId = quizId
        }
    }

    private class FakeStudyMaterialRepository : StudyMaterialRepository {
        var lastDeletedMaterialId: Long? = null
        private val materialsFlow = MutableStateFlow<List<StudyMaterialEntity>>(emptyList())

        override suspend fun insertMaterial(material: StudyMaterialEntity): Long = 1L
        override fun getAllMaterials(): Flow<List<StudyMaterialEntity>> = materialsFlow
        override suspend fun getMaterialById(materialId: Long): StudyMaterialEntity? = null
        override suspend fun getMaterialsByIds(materialIds: List<Long>): List<StudyMaterialEntity> = emptyList()
        override fun getMaterialsByFileType(fileType: String): Flow<List<StudyMaterialEntity>> = MutableStateFlow(emptyList())
        override suspend fun deleteMaterial(material: StudyMaterialEntity) {}
        override suspend fun deleteMaterialById(materialId: Long) {
            lastDeletedMaterialId = materialId
        }
    }
}
