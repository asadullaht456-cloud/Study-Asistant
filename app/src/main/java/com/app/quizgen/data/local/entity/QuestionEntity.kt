package com.app.quizgen.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores individual questions, options, model answers, and explanations
 * for a generated quiz.
 *
 * Table: questions
 * See: database_schema_specifications.md — Entity 4
 */
@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["quiz_id"],
            childColumns = ["quiz_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quiz_id"])]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "question_id")
    val questionId: Long = 0,

    @ColumnInfo(name = "quiz_id")
    val quizId: Long,

    @ColumnInfo(name = "question_type")
    val questionType: String, // "MCQ", "SHORT", "LONG"

    @ColumnInfo(name = "question_text")
    val questionText: String,

    @ColumnInfo(name = "options_json")
    val optionsJson: String?, // JSON array for MCQs (e.g. ["A","B","C","D"]); NULL for SHORT/LONG

    @ColumnInfo(name = "correct_answer")
    val correctAnswer: String,

    @ColumnInfo(name = "explanation")
    val explanation: String? // Detailed AI justification; nullable
)
