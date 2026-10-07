package com.app.quizgen.di

import android.content.Context
import androidx.room.Room
import com.app.quizgen.data.local.AppDatabase
import com.app.quizgen.data.local.dao.QuestionDao
import com.app.quizgen.data.local.dao.QuizDao
import com.app.quizgen.data.local.dao.QuizMaterialCrossRefDao
import com.app.quizgen.data.local.dao.StudyMaterialDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides Room Database and DAO instances as singletons.
 *
 * Implements Task 5.1 from dev_work_phases.md.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    @Singleton
    fun provideStudyMaterialDao(database: AppDatabase): StudyMaterialDao {
        return database.studyMaterialDao()
    }

    @Provides
    @Singleton
    fun provideQuizDao(database: AppDatabase): QuizDao {
        return database.quizDao()
    }

    @Provides
    @Singleton
    fun provideQuestionDao(database: AppDatabase): QuestionDao {
        return database.questionDao()
    }

    @Provides
    @Singleton
    fun provideQuizMaterialCrossRefDao(database: AppDatabase): QuizMaterialCrossRefDao {
        return database.quizMaterialCrossRefDao()
    }
}
