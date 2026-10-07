package com.app.quizgen.data.remote

import com.app.quizgen.data.local.entity.QuestionEntity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of parsing a Gemini AI response into a structured quiz title
 * and question entities.
 */
data class ParsedQuizResult(
    val title: String,
    val questions: List<QuestionEntity>
)

/**
 * Parses Gemini AI JSON responses into domain entities ([QuestionEntity]).
 *
 * Handles markdown fence stripping, JSON extraction from conversational wrappers,
 * schema variation resilience, and normalization of question types and options.
 *
 * Implements Task 4.3 from dev_work_phases.md.
 */
@Singleton
class GeminiResponseParser @Inject constructor() {

    companion object {
        private const val DEFAULT_QUIZ_TITLE = "AI Generated Quiz"
        private const val TYPE_MCQ = "MCQ"
        private const val TYPE_SHORT = "SHORT"
        private const val TYPE_LONG = "LONG"
    }

    /**
     * Parses the raw AI response text into [ParsedQuizResult].
     *
     * @param rawResponse The text returned by Gemini AI.
     * @param fallbackTitle Optional fallback title if none is found in the response.
     * @return Parsed quiz title and list of [QuestionEntity] objects.
     * @throws QuizParsingException if the response cannot be parsed into valid questions.
     */
    fun parse(
        rawResponse: String,
        fallbackTitle: String = DEFAULT_QUIZ_TITLE
    ): ParsedQuizResult {
        if (rawResponse.isBlank()) {
            throw QuizParsingException("AI response is empty or blank", rawResponse)
        }

        val cleanedJson = extractJsonPayload(rawResponse)
            ?: throw QuizParsingException("No valid JSON payload found in AI response", rawResponse)

        try {
            if (cleanedJson.startsWith("{")) {
                val rootObject = JSONObject(cleanedJson)
                val title = rootObject.optString("title").ifBlank {
                    rootObject.optString("quiz_title").ifBlank { fallbackTitle }
                }

                val questionsArray = rootObject.optJSONArray("questions")
                    ?: rootObject.optJSONArray("quiz")
                    ?: rootObject.optJSONArray("items")
                    ?: rootObject.optJSONArray("data")
                    ?: throw QuizParsingException("Root object missing 'questions' array", rawResponse)

                val questions = parseQuestionsArray(questionsArray, rawResponse)
                return ParsedQuizResult(title = title.trim(), questions = questions)
            } else if (cleanedJson.startsWith("[")) {
                val rootArray = JSONArray(cleanedJson)
                val questions = parseQuestionsArray(rootArray, rawResponse)
                return ParsedQuizResult(title = fallbackTitle, questions = questions)
            } else {
                throw QuizParsingException("Extracted payload is neither JSON Object nor Array", rawResponse)
            }
        } catch (e: QuizParsingException) {
            throw e
        } catch (e: Exception) {
            throw QuizParsingException("Failed to parse AI JSON response: ${e.message}", rawResponse, e)
        }
    }

    /**
     * Extracts pure JSON string by stripping markdown code blocks,
     * leading commentary, and trailing characters.
     */
    internal fun extractJsonPayload(raw: String): String? {
        var text = raw.trim()

        // Strip ```json ... ``` or ``` ... ``` code blocks
        if (text.contains("```")) {
            val fenceRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```", RegexOption.IGNORE_CASE)
            val match = fenceRegex.find(text)
            if (match != null) {
                text = match.groupValues[1].trim()
            }
        }

        val firstBrace = text.indexOf('{')
        val firstBracket = text.indexOf('[')

        // Determine whether object or array appears first
        val startIndex = when {
            firstBrace != -1 && firstBracket != -1 -> minOf(firstBrace, firstBracket)
            firstBrace != -1 -> firstBrace
            firstBracket != -1 -> firstBracket
            else -> return null
        }

        val isObject = startIndex == firstBrace
        val endIndex = if (isObject) {
            text.lastIndexOf('}')
        } else {
            text.lastIndexOf(']')
        }

        if (endIndex == -1 || endIndex <= startIndex) {
            return null
        }

        return text.substring(startIndex, endIndex + 1).trim()
    }

    private fun parseQuestionsArray(
        questionsArray: JSONArray,
        rawResponse: String
    ): List<QuestionEntity> {
        if (questionsArray.length() == 0) {
            throw QuizParsingException("The 'questions' array is empty", rawResponse)
        }

        val questions = mutableListOf<QuestionEntity>()

        for (i in 0 until questionsArray.length()) {
            val item = questionsArray.optJSONObject(i) ?: continue

            val questionText = item.optString("question_text").ifBlank {
                item.optString("question").ifBlank {
                    item.optString("prompt").ifBlank {
                        item.optString("text")
                    }
                }
            }.trim()

            if (questionText.isBlank()) {
                // Skip empty question items
                continue
            }

            val rawType = item.optString("question_type").ifBlank {
                item.optString("type")
            }

            val optionsList = extractOptionsList(item)
            val normalizedType = normalizeQuestionType(rawType, optionsList)

            val optionsJson = if (normalizedType == TYPE_MCQ && optionsList.isNotEmpty()) {
                JSONArray(optionsList).toString()
            } else {
                null
            }

            var correctAnswer = item.optString("correct_answer").ifBlank {
                item.optString("answer").ifBlank {
                    item.optString("model_answer").ifBlank {
                        item.optString("key")
                    }
                }
            }.trim()

            // If MCQ and answer is a letter index (e.g. "A", "B", "C", "D"), resolve to actual text if possible
            if (normalizedType == TYPE_MCQ && optionsList.isNotEmpty()) {
                correctAnswer = resolveMcqAnswer(correctAnswer, optionsList)
            }

            val explanation = item.optString("explanation").ifBlank {
                item.optString("rationale").ifBlank {
                    item.optString("reasoning")
                }
            }.trim().takeIf { it.isNotBlank() }

            questions.add(
                QuestionEntity(
                    questionId = 0,
                    quizId = 0,
                    questionType = normalizedType,
                    questionText = questionText,
                    optionsJson = optionsJson,
                    correctAnswer = correctAnswer,
                    explanation = explanation
                )
            )
        }

        if (questions.isEmpty()) {
            throw QuizParsingException("No valid questions could be extracted from response", rawResponse)
        }

        return questions
    }

    private fun extractOptionsList(item: JSONObject): List<String> {
        val optionsArray = item.optJSONArray("options")
            ?: item.optJSONArray("choices")
            ?: item.optJSONArray("answers")

        if (optionsArray != null) {
            val list = mutableListOf<String>()
            for (i in 0 until optionsArray.length()) {
                val opt = optionsArray.optString(i).trim()
                if (opt.isNotBlank()) list.add(opt)
            }
            return list
        }

        // Handle case where options are formatted as a JSONObject {"A": "...", "B": "..."}
        val optionsObj = item.optJSONObject("options")
            ?: item.optJSONObject("choices")

        if (optionsObj != null) {
            val list = mutableListOf<String>()
            val keys = optionsObj.keys().asSequence().toList().sorted()
            for (key in keys) {
                val value = optionsObj.optString(key).trim()
                if (value.isNotBlank()) list.add(value)
            }
            return list
        }

        return emptyList()
    }

    private fun normalizeQuestionType(rawType: String, optionsList: List<String>): String {
        val upper = rawType.uppercase().trim()
        return when {
            upper.contains("MCQ") || upper.contains("MULTIPLE") -> TYPE_MCQ
            upper.contains("SHORT") -> TYPE_SHORT
            upper.contains("LONG") || upper.contains("ESSAY") -> TYPE_LONG
            optionsList.size >= 2 -> TYPE_MCQ
            else -> TYPE_SHORT
        }
    }

    private fun resolveMcqAnswer(rawAnswer: String, options: List<String>): String {
        // Direct match with one of the options
        val directMatch = options.firstOrNull { it.equals(rawAnswer, ignoreCase = true) }
        if (directMatch != null) return directMatch

        // Handle letter references: "A", "B", "C", "D", "Option A", "Choice A"
        val letterPattern = Regex("(?:option|choice)?\\s*([A-D])\\b", RegexOption.IGNORE_CASE)
        val match = letterPattern.find(rawAnswer)
        if (match != null) {
            val letter = match.groupValues[1].uppercase()[0]
            val index = letter - 'A'
            if (index in options.indices) {
                return options[index]
            }
        }

        // Handle numeric references: "1", "2", "3", "4"
        val numPattern = Regex("(?:option|choice)?\\s*([1-4])\\b", RegexOption.IGNORE_CASE)
        val numMatch = numPattern.find(rawAnswer)
        if (numMatch != null) {
            val num = numMatch.groupValues[1].toInt()
            val index = num - 1
            if (index in options.indices) {
                return options[index]
            }
        }

        // Fallback: return raw answer
        return rawAnswer
    }
}

/**
 * Exception thrown when parsing AI response fails or yields invalid schema.
 */
class QuizParsingException(
    message: String,
    val rawResponse: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)
