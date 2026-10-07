package com.app.quizgen.data.remote

import com.app.quizgen.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client for interacting with the Google Gemini Generative AI API.
 *
 * Configures the GenerativeModel with API credentials, safety settings,
 * generation parameters, and JSON output specification.
 *
 * Implements Task 4.1 from dev_work_phases.md.
 */
@Singleton
open class GeminiClient @Inject constructor() {

    companion object {
        const val DEFAULT_MODEL = "gemini-1.5-flash"
        const val DEFAULT_TEMPERATURE = 0.4f
        const val DEFAULT_TOP_K = 32
        const val DEFAULT_TOP_P = 0.95f
    }

    @Volatile
    private var customApiKey: String? = null

    @Volatile
    private var activeModelName: String = DEFAULT_MODEL

    /**
     * Sets or overrides the Gemini API key at runtime.
     */
    open fun setApiKey(apiKey: String) {
        customApiKey = apiKey.trim()
    }

    /**
     * Retrieves the active API key, preferring any runtime-set key
     * over the build configuration key.
     */
    open fun getApiKey(): String {
        return customApiKey?.takeIf { it.isNotBlank() }
            ?: BuildConfig.GEMINI_API_KEY
    }

    /**
     * Configures the Gemini model name to use (e.g., "gemini-1.5-flash", "gemini-1.5-pro").
     */
    open fun setModelName(modelName: String) {
        require(modelName.isNotBlank()) { "Model name cannot be blank" }
        activeModelName = modelName.trim()
    }

    /**
     * Returns the currently configured model name.
     */
    open fun getModelName(): String = activeModelName

    /**
     * Generates text content from Gemini using the provided prompt.
     *
     * @param prompt The complete prompt text including instructions and source text.
     * @return The raw response text returned by the model.
     * @throws GeminiApiException if the API key is missing, generation fails, or output is empty.
     */
    open suspend fun generateContent(prompt: String): String {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            throw GeminiApiException(
                "Gemini API key is not configured. Please supply a valid key via BuildConfig.GEMINI_API_KEY or GeminiClient.setApiKey()."
            )
        }

        return withContext(Dispatchers.IO) {
            try {
                val model = createGenerativeModel(apiKey, activeModelName)
                val response = model.generateContent(prompt)
                val text = response.text

                if (text.isNullOrBlank()) {
                    throw GeminiApiException("Gemini API returned an empty or blank response.")
                }

                text
            } catch (e: GeminiApiException) {
                throw e
            } catch (e: Exception) {
                throw GeminiApiException("Gemini API call failed: ${e.message}", e)
            }
        }
    }

    /**
     * Factory function to create a configured [GenerativeModel] instance.
     */
    internal fun createGenerativeModel(
        apiKey: String,
        modelName: String
    ): GenerativeModel {
        val config = generationConfig {
            temperature = DEFAULT_TEMPERATURE
            topK = DEFAULT_TOP_K
            topP = DEFAULT_TOP_P
            responseMimeType = "application/json"
        }

        val safetySettings = listOf(
            SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE),
            SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE)
        )

        return GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = config,
            safetySettings = safetySettings
        )
    }
}

/**
 * Exception thrown when Gemini API client interactions fail.
 */
class GeminiApiException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
