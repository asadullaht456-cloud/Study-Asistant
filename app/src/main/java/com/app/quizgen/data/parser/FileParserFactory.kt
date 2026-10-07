package com.app.quizgen.data.parser

import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory that dispatches to the correct [TextExtractor] implementation
 * based on the file type (PDF, DOCX, TXT).
 *
 * Injected via Hilt with all three parser implementations.
 */
@Singleton
class FileParserFactory @Inject constructor(
    private val pdfTextExtractor: PdfTextExtractor,
    private val docxTextExtractor: DocxTextExtractor,
    private val txtTextExtractor: TxtTextExtractor
) {

    /**
     * Returns the appropriate [TextExtractor] for the given file type.
     *
     * @param fileType The file extension/type string: "PDF", "DOCX", or "TXT" (case-insensitive).
     * @return The matching [TextExtractor] implementation.
     * @throws TextExtractionException if the file type is not supported.
     */
    fun getExtractor(fileType: String): TextExtractor {
        return when (fileType.uppercase()) {
            "PDF" -> pdfTextExtractor
            "DOCX" -> docxTextExtractor
            "TXT" -> txtTextExtractor
            else -> throw TextExtractionException("Unsupported file type: $fileType. Supported: PDF, DOCX, TXT")
        }
    }

    /**
     * Convenience method: extracts text from a file in one call.
     *
     * @param fileType The file type string.
     * @param inputStream The file's input stream.
     * @return Extracted plain text.
     */
    suspend fun extractText(fileType: String, inputStream: InputStream): String {
        val extractor = getExtractor(fileType)
        return extractor.extractText(inputStream)
    }
}
