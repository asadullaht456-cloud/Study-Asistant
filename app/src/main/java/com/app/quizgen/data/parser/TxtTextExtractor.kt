package com.app.quizgen.data.parser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts plain text from TXT files with charset detection.
 *
 * Attempts to read with UTF-8 encoding by default. Falls back to
 * the platform's default charset if UTF-8 decoding produces
 * replacement characters, indicating a different encoding.
 */
@Singleton
class TxtTextExtractor @Inject constructor() : TextExtractor {

    override suspend fun extractText(inputStream: InputStream): String {
        return withContext(Dispatchers.IO) {
            try {
                val bytes = inputStream.readBytes()

                // Try UTF-8 first
                var text = String(bytes, Charsets.UTF_8)

                // If UTF-8 produces replacement characters, try default charset
                if (text.contains('\uFFFD') && Charset.defaultCharset() != Charsets.UTF_8) {
                    text = String(bytes, Charset.defaultCharset())
                }

                val extractedText = text.trim()
                if (extractedText.isEmpty()) {
                    throw TextExtractionException("TXT file is empty — no text content found.")
                }
                extractedText
            } catch (e: TextExtractionException) {
                throw e
            } catch (e: Exception) {
                throw TextExtractionException("Failed to read TXT file: ${e.message}", e)
            }
        }
    }
}
