package com.app.quizgen.data.remote

import com.app.quizgen.data.local.entity.StudyMaterialEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PromptBuilderTest {

    private lateinit var promptBuilder: PromptBuilder

    @Before
    fun setUp() {
        promptBuilder = PromptBuilder()
    }

    @Test
    fun `buildQuizPrompt includes material titles and extracted text`() {
        val materials = listOf(
            StudyMaterialEntity(
                material_id = 1L,
                title = "Biology_Chapter1.pdf",
                file_type = "PDF",
                file_uri = "file:///data/1.pdf",
                extracted_text = "Photosynthesis is the process used by plants.",
                file_size_mb = 2.5,
                uploaded_at = 1000L
            ),
            StudyMaterialEntity(
                material_id = 2L,
                title = "Chemistry_Notes.txt",
                file_type = "TXT",
                file_uri = "file:///data/2.txt",
                extracted_text = "Molecules are made of atoms bonded together.",
                file_size_mb = 0.5,
                uploaded_at = 2000L
            )
        )

        val prompt = promptBuilder.buildQuizPrompt(
            materials = materials,
            difficulty = "EASY",
            mcqCount = 5,
            shortQCount = 2,
            longQCount = 1
        )

        assertTrue(prompt.contains("Biology_Chapter1.pdf"))
        assertTrue(prompt.contains("Photosynthesis is the process used by plants."))
        assertTrue(prompt.contains("Chemistry_Notes.txt"))
        assertTrue(prompt.contains("Molecules are made of atoms bonded together."))
        assertTrue(prompt.contains("EASY"))
        assertTrue(prompt.contains("5"))
        assertTrue(prompt.contains("2"))
        assertTrue(prompt.contains("1"))
        assertTrue(prompt.contains("Total Questions Required: 8"))
    }

    @Test
    fun `buildQuizPrompt sets appropriate difficulty guidelines`() {
        val promptEasy = promptBuilder.buildQuizPromptFromText(
            sourceText = "Simple concepts",
            difficulty = "EASY",
            mcqCount = 1,
            shortQCount = 0,
            longQCount = 0
        )
        assertTrue(promptEasy.contains("direct recall") || promptEasy.contains("foundational definitions"))

        val promptHard = promptBuilder.buildQuizPromptFromText(
            sourceText = "Advanced concepts",
            difficulty = "HARD",
            mcqCount = 1,
            shortQCount = 0,
            longQCount = 0
        )
        assertTrue(promptHard.contains("deep synthesis") || promptHard.contains("critical analysis"))
    }

    @Test
    fun `buildQuizPrompt excludes instructions for zero-count question types`() {
        val prompt = promptBuilder.buildQuizPromptFromText(
            sourceText = "Some sample study text",
            difficulty = "MEDIUM",
            mcqCount = 5,
            shortQCount = 0,
            longQCount = 0
        )

        assertTrue(prompt.contains("Multiple Choice Questions (MCQ): exactly 5"))
        assertFalse(prompt.contains("Short Answer Questions (SHORT): exactly 0"))
        assertFalse(prompt.contains("Long Answer / Essay Questions (LONG): exactly 0"))
    }
}
