package com.app.quizgen.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores metadata and parsed text content from files uploaded by the user.
 *
 * Table: study_materials
 * See: database_schema_specifications.md — Entity 1
 */
@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "material_id")
    val materialId: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "file_type")
    val fileType: String, // "PDF", "DOCX", "TXT"

    @ColumnInfo(name = "file_uri")
    val fileUri: String,

    @ColumnInfo(name = "extracted_text")
    val extractedText: String,

    @ColumnInfo(name = "file_size_mb")
    val fileSizeMb: Double,

    @ColumnInfo(name = "uploaded_at")
    val uploadedAt: Long // Epoch timestamp
)
