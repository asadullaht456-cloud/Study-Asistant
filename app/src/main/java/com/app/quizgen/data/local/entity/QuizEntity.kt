package com.app.quizgen.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores overall metadata and configurations for each generated quiz.
 *
 * Table: quizzes
 * See: database_schema_specifications.md — Entity 2
 */
@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "quiz_id")
    val quizId: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "difficulty_level")
    val difficultyLevel: String, // "EASY", "MEDIUM", "HARD"

    @ColumnInfo(name = "total_questions")
    val totalQuestions: Int,

    @ColumnInfo(name = "mcq_count", defaultValue = "0")
    val mcqCount: Int = 0,

    @ColumnInfo(name = "short_q_count", defaultValue = "0")
    val shortQCount: Int = 0,

    @ColumnInfo(name = "long_q_count", defaultValue = "0")
    val longQCount: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long // Epoch timestamp
)
