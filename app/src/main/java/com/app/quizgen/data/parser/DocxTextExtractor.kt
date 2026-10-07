package com.app.quizgen.data.parser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts plain text from DOCX files using Apache POI.
 *
 * Reads all paragraphs and tables from the document and
 * concatenates their text content with newline separators.
 */
@Singleton
class DocxTextExtractor @Inject constructor() : TextExtractor {

    override suspend fun extractText(inputStream: InputStream): String {
        return withContext(Dispatchers.IO) {
            try {
                val document = XWPFDocument(inputStream)
                val textBuilder = StringBuilder()

                // Extract text from paragraphs
                for (paragraph in document.paragraphs) {
                    val text = paragraph.text?.trim()
                    if (!text.isNullOrEmpty()) {
                        textBuilder.appendLine(text)
                    }
                }

                // Extract text from tables
                for (table in document.tables) {
                    for (row in table.rows) {
                        val rowText = row.tableCells.joinToString(" | ") { cell ->
                            cell.text?.trim() ?: ""
                        }
                        if (rowText.isNotBlank()) {
                            textBuilder.appendLine(rowText)
                        }
                    }
                }

                document.close()

                val extractedText = textBuilder.toString().trim()
                if (extractedText.isEmpty()) {
                    throw TextExtractionException("DOCX file contains no extractable text content.")
                }
                extractedText
            } catch (e: TextExtractionException) {
                throw e
            } catch (e: Exception) {
                throw TextExtractionException("Failed to extract text from DOCX: ${e.message}", e)
            }
        }
    }
}
