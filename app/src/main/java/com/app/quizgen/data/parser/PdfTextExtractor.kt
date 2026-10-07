package com.app.quizgen.data.parser

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor as ITextPdfExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Extracts plain text from PDF files using the iText 7 library.
 *
 * Reads all pages sequentially and concatenates their text content
 * with newline separators between pages.
 */
@Singleton
class PdfTextExtractor @Inject constructor() : TextExtractor {

    override suspend fun extractText(inputStream: InputStream): String {
        return withContext(Dispatchers.IO) {
            try {
                val reader = PdfReader(inputStream)
                val pdfDocument = PdfDocument(reader)
                val textBuilder = StringBuilder()

                for (pageNum in 1..pdfDocument.numberOfPages) {
                    val page = pdfDocument.getPage(pageNum)
                    val pageText = ITextPdfExtractor.getTextFromPage(page)
                    textBuilder.append(pageText)
                    if (pageNum < pdfDocument.numberOfPages) {
                        textBuilder.append("\n\n")
                    }
                }

                pdfDocument.close()
                reader.close()

                val extractedText = textBuilder.toString().trim()
                if (extractedText.isEmpty()) {
                    throw TextExtractionException("PDF file contains no extractable text content.")
                }
                extractedText
            } catch (e: TextExtractionException) {
                throw e
            } catch (e: Exception) {
                throw TextExtractionException("Failed to extract text from PDF: ${e.message}", e)
            }
        }
    }
}
