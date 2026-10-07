package com.app.quizgen.data.parser

/**
 * Common interface for all document text extractors.
 *
 * Each parser implementation handles a specific file format
 * (PDF, DOCX, TXT) and extracts plain text content from it.
 */
interface TextExtractor {

    /**
     * Extracts plain text content from the given file input stream.
     *
     * @param inputStream The file's input stream.
     * @return Extracted plain text content.
     * @throws TextExtractionException if extraction fails.
     */
    suspend fun extractText(inputStream: java.io.InputStream): String
}

/**
 * Exception thrown when text extraction from a document fails.
 */
class TextExtractionException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
