package com.app.quizgen.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction entity mapping the many-to-many relationship between Quiz and StudyMaterial.
 * A single quiz can be generated from multiple study materials, and a single material
 * can be used across multiple quizzes.
 *
 * Table: quiz_material_cross_ref
 * Composite PK: (quiz_id, material_id)
 * See: database_schema_specifications.md — Entity 3
 */
@Entity(
    tableName = "quiz_material_cross_ref",
    primaryKeys = ["quiz_id", "material_id"],
    foreignKeys = [
        ForeignKey(
            entity = QuizEntity::class,
            parentColumns = ["quiz_id"],
            childColumns = ["quiz_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudyMaterialEntity::class,
            parentColumns = ["material_id"],
            childColumns = ["material_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["quiz_id"]),
        Index(value = ["material_id"])
    ]
)
data class QuizMaterialCrossRef(
    @ColumnInfo(name = "quiz_id")
    val quizId: Long,

    @ColumnInfo(name = "material_id")
    val materialId: Long
)
