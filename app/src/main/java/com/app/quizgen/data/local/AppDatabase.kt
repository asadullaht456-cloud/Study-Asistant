package com.app.quizgen.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.quizgen.data.local.converter.Converters
import com.app.quizgen.data.local.dao.QuestionDao
import com.app.quizgen.data.local.dao.QuizDao
import com.app.quizgen.data.local.dao.QuizMaterialCrossRefDao
import com.app.quizgen.data.local.dao.StudyMaterialDao
import com.app.quizgen.data.local.entity.QuestionEntity
import com.app.quizgen.data.local.entity.QuizEntity
import com.app.quizgen.data.local.entity.QuizMaterialCrossRef
import com.app.quizgen.data.local.entity.StudyMaterialEntity

/**
 * Room Database class for the AI Quiz Generator app.
 *
 * Database file: ai_quiz_generator.db
 * Registered entities: StudyMaterialEntity, QuizEntity, QuizMaterialCrossRef, QuestionEntity
 * Type converters: Converters (JSON ↔ List<String>)
 *
 * See: database_schema_specifications.md
 */
@Database(
    entities = [
        StudyMaterialEntity::class,
        QuizEntity::class,
        QuizMaterialCrossRef::class,
        QuestionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyMaterialDao(): StudyMaterialDao
    abstract fun quizDao(): QuizDao
    abstract fun questionDao(): QuestionDao
    abstract fun quizMaterialCrossRefDao(): QuizMaterialCrossRefDao

    companion object {
        const val DATABASE_NAME = "ai_quiz_generator.db"
    }
}
