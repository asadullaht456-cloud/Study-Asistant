package com.app.quizgen.util

/**
 * Utility for validating uploaded files before storage or parsing.
 *
 * Enforces the 15MB file size constraint as defined in:
 * - database_schema_specifications.md — Section 5, Rule 1
 * - ui_ux_requirements_specifications.md — Section 5, Rule 1
 */
object FileValidator {

    /** Maximum allowed file size in megabytes. */
    const val MAX_FILE_SIZE_MB = 15.0

    /** Maximum allowed file size in bytes (15 MB). */
    const val MAX_FILE_SIZE_BYTES = (MAX_FILE_SIZE_MB * 1024 * 1024).toLong()

    /** Supported file extensions. */
    val SUPPORTED_EXTENSIONS = setOf("pdf", "docx", "txt")

    /**
     * Validates file size against the 15MB limit.
     *
     * @param fileSizeBytes The file size in bytes.
     * @return [FileValidationResult.Valid] if within limits,
     *         [FileValidationResult.FileTooLarge] if exceeds 15MB.
     */
    fun validateFileSize(fileSizeBytes: Long): FileValidationResult {
        val fileSizeMb = fileSizeBytes.toDouble() / (1024 * 1024)
        return if (fileSizeMb <= MAX_FILE_SIZE_MB) {
            FileValidationResult.Valid(fileSizeMb)
        } else {
            FileValidationResult.FileTooLarge(fileSizeMb)
        }
    }

    /**
     * Validates the file extension is one of the supported types.
     *
     * @param fileName The file name or path.
     * @return [FileValidationResult.Valid] if supported,
     *         [FileValidationResult.UnsupportedFormat] if not.
     */
    fun validateFileType(fileName: String): FileValidationResult {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return if (extension in SUPPORTED_EXTENSIONS) {
            FileValidationResult.Valid(0.0)
        } else {
            FileValidationResult.UnsupportedFormat(extension)
        }
    }

    /**
     * Runs all validations (size + type) in sequence.
     *
     * @param fileName The file name for type checking.
     * @param fileSizeBytes The file size in bytes for size checking.
     * @return First failing [FileValidationResult], or [FileValidationResult.Valid].
     */
    fun validateFile(fileName: String, fileSizeBytes: Long): FileValidationResult {
        val typeResult = validateFileType(fileName)
        if (typeResult !is FileValidationResult.Valid) return typeResult

        return validateFileSize(fileSizeBytes)
    }
}

/**
 * Sealed class representing the result of file validation.
 */
sealed class FileValidationResult {
    /** File passed validation. [fileSizeMb] contains the calculated size. */
    data class Valid(val fileSizeMb: Double) : FileValidationResult()

    /** File exceeds the 15MB limit. */
    data class FileTooLarge(val actualSizeMb: Double) : FileValidationResult() {
        val message: String
            get() = "File size exceeds 15MB limit (${String.format("%.1f", actualSizeMb)} MB)"
    }

    /** File type is not supported (not PDF, DOCX, or TXT). */
    data class UnsupportedFormat(val extension: String) : FileValidationResult() {
        val message: String
            get() = "Unsupported file format: .$extension. Supported: PDF, DOCX, TXT"
    }
}
