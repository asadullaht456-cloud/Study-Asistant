package com.app.quizgen.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeminiResponseParserTest {

    private lateinit var parser: GeminiResponseParser

    @Before
    fun setUp() {
        parser = GeminiResponseParser()
    }

    @Test
    fun `parse cleanly handles valid JSON with MCQ, SHORT, and LONG questions`() {
        val json = """
        {
          "title": "Photosynthesis Fundamentals",
          "questions": [
            {
              "question_type": "MCQ",
              "question_text": "What pigment absorbs light in plant chloroplasts?",
              "options": ["Chlorophyll", "Carotenoid", "Hemoglobin", "Melanin"],
              "correct_answer": "Chlorophyll",
              "explanation": "Chlorophyll is the primary pigment responsible for light absorption."
            },
            {
              "question_type": "SHORT",
              "question_text": "Define the light-dependent reactions of photosynthesis.",
              "options": null,
              "correct_answer": "Reactions converting solar energy into chemical energy (ATP and NADPH).",
              "explanation": "Must mention solar energy conversion into ATP/NADPH."
            },
            {
              "question_type": "LONG",
              "question_text": "Explain the Calvin cycle in detail, including key inputs and outputs.",
              "options": null,
              "correct_answer": "The Calvin cycle fixes CO2 using ATP and NADPH to produce G3P sugar molecules...",
              "explanation": "Must detail carbon fixation, reduction phase, and regeneration of RuBP."
            }
          ]
        }
        """.trimIndent()

        val result = parser.parse(json)

        assertEquals("Photosynthesis Fundamentals", result.title)
        assertEquals(3, result.questions.size)

        // Question 1: MCQ
        val mcq = result.questions[0]
        assertEquals("MCQ", mcq.questionType)
        assertEquals("What pigment absorbs light in plant chloroplasts?", mcq.questionText)
        assertNotNull(mcq.optionsJson)
        assertTrue(mcq.optionsJson!!.contains("Chlorophyll"))
        assertEquals("Chlorophyll", mcq.correctAnswer)
        assertEquals("Chlorophyll is the primary pigment responsible for light absorption.", mcq.explanation)

        // Question 2: SHORT
        val shortQ = result.questions[1]
        assertEquals("SHORT", shortQ.questionType)
        assertNull(shortQ.optionsJson)
        assertTrue(shortQ.correctAnswer.contains("ATP and NADPH"))

        // Question 3: LONG
        val longQ = result.questions[2]
        assertEquals("LONG", longQ.questionType)
        assertNull(longQ.optionsJson)
        assertTrue(longQ.correctAnswer.contains("Calvin cycle"))
    }

    @Test
    fun `parse strips markdown code fences successfully`() {
        val markdownWrapped = """
        Here is the requested quiz in JSON format:
        ```json
        {
          "title": "Wrapped Quiz",
          "questions": [
            {
              "question_type": "MCQ",
              "question_text": "What is H2O?",
              "options": ["Water", "Hydrogen peroxide", "Oxygen", "Salt"],
              "correct_answer": "Water",
              "explanation": "H2O is water."
            }
          ]
        }
        ```
        Hope this helps!
        """.trimIndent()

        val result = parser.parse(markdownWrapped)
        assertEquals("Wrapped Quiz", result.title)
        assertEquals(1, result.questions.size)
        assertEquals("Water", result.questions[0].correctAnswer)
    }

    @Test
    fun `parse resolves MCQ letter option index to option text`() {
        val json = """
        {
          "title": "Letter Index Quiz",
          "questions": [
            {
              "question_type": "MCQ",
              "question_text": "What is the capital of France?",
              "options": ["London", "Berlin", "Paris", "Madrid"],
              "correct_answer": "C",
              "explanation": "Paris is the capital of France."
            }
          ]
        }
        """.trimIndent()

        val result = parser.parse(json)
        assertEquals(1, result.questions.size)
        assertEquals("Paris", result.questions[0].correctAnswer)
    }

    @Test
    fun `parse supports direct JSON array of questions`() {
        val arrayJson = """
        [
          {
            "question_type": "SHORT",
            "question_text": "What is Newton's first law?",
            "options": null,
            "correct_answer": "An object in motion stays in motion unless acted upon by an external force.",
            "explanation": "Law of inertia."
          }
        ]
        """.trimIndent()

        val result = parser.parse(arrayJson, fallbackTitle = "Physics Exam")
        assertEquals("Physics Exam", result.title)
        assertEquals(1, result.questions.size)
        assertEquals("SHORT", result.questions[0].questionType)
    }

    @Test(expected = QuizParsingException::class)
    fun `parse throws QuizParsingException on empty input`() {
        parser.parse("")
    }

    @Test(expected = QuizParsingException::class)
    fun `parse throws QuizParsingException on malformed non-JSON string`() {
        parser.parse("This is just plain conversational text without JSON.")
    }

    @Test(expected = QuizParsingException::class)
    fun `parse throws QuizParsingException when questions array is empty`() {
        val emptyQuestionsJson = """
        {
          "title": "Empty Quiz",
          "questions": []
        }
        """.trimIndent()

        parser.parse(emptyQuestionsJson)
    }
}
