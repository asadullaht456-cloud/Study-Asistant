package com.app.quizgen.data.storage

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages file storage in the app's internal storage directory.
 *
 * Uploaded files are copied to: app-internal-storage/uploaded_documents/
 * Each file gets a unique name to prevent collisions.
 *
 * Storage path: Android/data/com.app.quizgen/files/uploaded_documents/
 * See: database_schema_specifications.md — Section 1 (Storage Strategy)
 */
@Singleton
class FileStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val UPLOAD_DIR = "uploaded_documents"
    }

    /**
     * Copies an uploaded file from a content URI to app-internal storage.
     *
     * The file is saved with a unique name (UUID prefix) to prevent collisions
     * when multiple files share the same original name.
     *
     * @param contentUri The content:// URI from the file picker.
     * @param originalFileName The original file name for preserving the extension.
     * @return The local file URI (file:// path) pointing to the stored copy.
     * @throws FileStorageException if the copy operation fails.
     */
    suspend fun saveFile(contentUri: Uri, originalFileName: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val uploadDir = getUploadDirectory()
                val uniqueFileName = "${UUID.randomUUID()}_$originalFileName"
                val destinationFile = File(uploadDir, uniqueFileName)

                val inputStream: InputStream = context.contentResolver.openInputStream(contentUri)
                    ?: throw FileStorageException("Cannot open file: $contentUri")

                inputStream.use { input ->
                    destinationFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                destinationFile.toURI().toString()
            } catch (e: FileStorageException) {
                throw e
            } catch (e: Exception) {
                throw FileStorageException("Failed to save file: ${e.message}", e)
            }
        }
    }

    /**
     * Opens an input stream for a stored file by its local URI.
     *
     * @param fileUri The local file URI string (as stored in StudyMaterialEntity.fileUri).
     * @return An [InputStream] for reading the file content.
     */
    suspend fun openFile(fileUri: String): InputStream {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(java.net.URI(fileUri))
                if (!file.exists()) {
                    throw FileStorageException("File not found: $fileUri")
                }
                file.inputStream()
            } catch (e: FileStorageException) {
                throw e
            } catch (e: Exception) {
                throw FileStorageException("Failed to open file: ${e.message}", e)
            }
        }
    }

    /**
     * Deletes a stored file by its local URI.
     *
     * @param fileUri The local file URI string.
     * @return true if the file was successfully deleted, false otherwise.
     */
    suspend fun deleteFile(fileUri: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(java.net.URI(fileUri))
                file.delete()
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Gets the file size in bytes from a content URI.
     *
     * @param contentUri The content:// URI from the file picker.
     * @return File size in bytes, or -1 if it cannot be determined.
     */
    fun getFileSize(contentUri: Uri): Long {
        return try {
            context.contentResolver.openAssetFileDescriptor(contentUri, "r")?.use {
                it.length
            } ?: -1L
        } catch (e: Exception) {
            -1L
        }
    }

    /**
     * Extracts the file name from a content URI.
     *
     * @param contentUri The content:// URI.
     * @return The display name of the file, or "unknown_file" if not found.
     */
    fun getFileName(contentUri: Uri): String {
        val cursor = context.contentResolver.query(contentUri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else "unknown_file"
            } else {
                "unknown_file"
            }
        } ?: "unknown_file"
    }

    /**
     * Ensures the upload directory exists, creating it if needed.
     */
    private fun getUploadDirectory(): File {
        val dir = File(context.filesDir, UPLOAD_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}

/**
 * Exception thrown when file storage operations fail.
 */
class FileStorageException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
