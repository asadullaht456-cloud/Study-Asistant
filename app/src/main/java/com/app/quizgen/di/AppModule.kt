package com.app.quizgen.di

import com.app.quizgen.data.repository.QuizRepository
import com.app.quizgen.data.repository.StudyMaterialRepository
import com.app.quizgen.data.repository.impl.QuizRepositoryImpl
import com.app.quizgen.data.repository.impl.StudyMaterialRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository interfaces to their concrete implementations.
 *
 * Implements Task 5.1 from dev_work_phases.md.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindStudyMaterialRepository(
        impl: StudyMaterialRepositoryImpl
    ): StudyMaterialRepository

    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        impl: QuizRepositoryImpl
    ): QuizRepository
}
