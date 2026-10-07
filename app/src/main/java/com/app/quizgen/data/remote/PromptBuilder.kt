package com.app.quizgen.data.remote

import com.app.quizgen.data.local.entity.StudyMaterialEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds dynamic system and user prompts instructing Gemini AI to generate
 * structured quizzes from extracted document text.
 *
 * Implements Task 4.2 from dev_work_phases.md.
 */
@Singleton
class PromptBuilder @Inject constructor() {

    companion object {
        const val MAX_SOURCE_TEXT_LENGTH = 150_000
    }

    /**
     * Builds the complete prompt for quiz generation.
     *
     * @param materials List of source [StudyMaterialEntity] objects whose extracted text will be used.
     * @param difficulty Difficulty level: "EASY", "MEDIUM", or "HARD".
     * @param mcqCount Number of Multiple Choice Questions requested.
     * @param shortQCount Number of Short-Answer Questions requested.
     * @param longQCount Number of Long-Answer Questions requested.
     * @return Formatted prompt string ready for submission to Gemini AI.
     */
    fun buildQuizPrompt(
        materials: List<StudyMaterialEntity>,
        difficulty: String,
        mcqCount: Int,
        shortQCount: Int,
        longQCount: Int
    ): String {
        val combinedText = materials.joinToString("\n\n") { material ->
            "--- SOURCE DOCUMENT: ${material.title} (${material.fileType}) ---\n${material.extractedText.trim()}"
        }

        return buildQuizPromptFromText(
            sourceText = combinedText,
            difficulty = difficulty,
            mcqCount = mcqCount,
            shortQCount = shortQCount,
            longQCount = longQCount
        )
    }

    /**
     * Builds the complete prompt directly from raw source text.
     *
     * @param sourceText Combined plain text from study materials.
     * @param difficulty Difficulty level: "EASY", "MEDIUM", or "HARD".
     * @param mcqCount Number of Multiple Choice Questions requested.
     * @param shortQCount Number of Short-Answer Questions requested.
     * @param longQCount Number of Long-Answer Questions requested.
     * @return Formatted prompt string.
     */
    fun buildQuizPromptFromText(
        sourceText: String,
        difficulty: String,
        mcqCount: Int,
        shortQCount: Int,
        longQCount: Int
    ): String {
        val totalQuestions = mcqCount + shortQCount + longQCount
        val normalizedDifficulty = difficulty.trim().uppercase()
        val difficultyGuideline = getDifficultyGuideline(normalizedDifficulty)

        val boundedText = if (sourceText.length > MAX_SOURCE_TEXT_LENGTH) {
            sourceText.substring(0, MAX_SOURCE_TEXT_LENGTH) + "\n\n[... Text truncated for model context limit ...]"
        } else {
            sourceText
        }

        val questionTypeInstructions = buildList {
            if (mcqCount > 0) {
                add("- Multiple Choice Questions (MCQ): exactly $mcqCount questions. Each MCQ must provide exactly 4 distinct options, with 1 unambiguous correct answer and a pedagogical explanation.")
            }
            if (shortQCount > 0) {
                add("- Short Answer Questions (SHORT): exactly $shortQCount questions. Each must have a concise model answer (2-4 sentences) and an explanation highlighting key points.")
            }
            if (longQCount > 0) {
                add("- Long Answer / Essay Questions (LONG): exactly $longQCount questions. Each must have a comprehensive model answer (multi-paragraph or bulleted breakdown) and scoring rationale.")
            }
        }.joinToString("\n")

        return """
You are an expert academic examiner and curriculum specialist.
Your task is to generate a comprehensive, high-quality educational quiz based STRICTLY on the source text provided below.

### QUIZ CONFIGURATION:
- Difficulty Level: $normalizedDifficulty
$difficultyGuideline
- Total Questions Required: $totalQuestions
$questionTypeInstructions

### STRICT OUTPUT FORMAT:
You MUST respond with a single, valid JSON object conforming to the following structure. Do NOT include markdown code fences, headers, or any text outside the JSON object.

{
  "title": "Descriptive Quiz Title based on Content",
  "questions": [
    {
      "question_type": "MCQ",
      "question_text": "Question prompt here?",
      "options": ["Option A text", "Option B text", "Option C text", "Option D text"],
      "correct_answer": "Exact matching string from options",
      "explanation": "Clear explanation of why this option is correct and others are not."
    },
    {
      "question_type": "SHORT",
      "question_text": "Short answer question prompt here?",
      "options": null,
      "correct_answer": "Concise model answer (2-4 sentences).",
      "explanation": "Key concepts and reasoning expected in a full-credit answer."
    },
    {
      "question_type": "LONG",
      "question_text": "In-depth analytical or essay question prompt here?",
      "options": null,
      "correct_answer": "Comprehensive model answer outlining main points, examples, and synthesis.",
      "explanation": "Evaluation criteria and detailed rationale."
    }
  ]
}

### CRITICAL RULES:
1. Grounding: All questions, correct answers, and distractors must be strictly grounded in the provided source text. Do NOT invent facts.
2. Exact Question Counts: Deliver exactly $mcqCount MCQs, $shortQCount SHORT questions, and $longQCount LONG questions (total: $totalQuestions).
3. MCQ Options:
   - Must have exactly 4 choices in the "options" array.
   - "correct_answer" MUST exactly match one of the strings in "options".
   - Distractors must be plausible but definitively incorrect based on the text.
4. Non-MCQ Types: For "SHORT" and "LONG", set "options" to null.
5. Format Compliance: Return ONLY valid, parseable JSON.

### SOURCE TEXT:
$boundedText
""".trimIndent()
    }

    private fun getDifficultyGuideline(difficulty: String): String {
        return when (difficulty) {
            "EASY" -> "  * Focus on direct recall, foundational definitions, explicitly stated facts, and core vocabulary."
            "HARD" -> "  * Focus on deep synthesis, multi-concept integration, edge cases, critical analysis, and rigorous problem-solving."
            else -> "  * Focus on conceptual understanding, practical application, comparing and contrasting ideas, and intermediate analysis."
        }
    }
}
