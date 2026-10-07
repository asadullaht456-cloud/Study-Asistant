package com.app.quizgen.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.app.quizgen.data.local.entity.QuestionEntity
import com.app.quizgen.data.local.entity.QuizEntity
import com.app.quizgen.data.local.entity.QuizMaterialCrossRef
import com.app.quizgen.data.local.entity.StudyMaterialEntity

/**
 * Relational data class representing a Quiz along with all its
 * generated Questions and the source StudyMaterials used to create it.
 *
 * Uses Room's @Relation and @Junction annotations:
 * - Quiz → Questions: One-to-Many via quiz_id FK
 * - Quiz → StudyMaterials: Many-to-Many via QuizMaterialCrossRef junction
 *
 * See: database_schema_specifications.md — Section 4 (Relational Room Queries)
 */
data class QuizWithQuestionsAndMaterials(
    @Embedded
    val quiz: QuizEntity,

    @Relation(
        parentColumn = "quiz_id",
        entityColumn = "quiz_id"
    )
    val questions: List<QuestionEntity>,

    @Relation(
        parentColumn = "quiz_id",
        entityColumn = "material_id",
        associateBy = Junction(
            value = QuizMaterialCrossRef::class,
            parentColumn = "quiz_id",
            entityColumn = "material_id"
        )
    )
    val sourceMaterials: List<StudyMaterialEntity>
)
