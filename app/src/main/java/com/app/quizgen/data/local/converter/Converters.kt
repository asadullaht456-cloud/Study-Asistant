package com.app.quizgen.data.local.converter

import androidx.room.TypeConverter
import org.json.JSONArray

/**
 * Room Type Converters for serializing/deserializing complex types.
 *
 * Handles JSON string ↔ List<String> conversion for the options_json
 * field in QuestionEntity (MCQ options stored as JSON array).
 */
class Converters {

    /**
     * Converts a JSON string array (e.g., ["Option A","Option B","Option C","Option D"])
     * to a Kotlin List<String>.
     */
    @TypeConverter
    fun fromJsonString(value: String?): List<String>? {
        if (value == null) return null
        return try {
            val jsonArray = JSONArray(value)
            List(jsonArray.length()) { index ->
                jsonArray.getString(index)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converts a Kotlin List<String> to a JSON string array for Room storage.
     */
    @TypeConverter
    fun toJsonString(list: List<String>?): String? {
        if (list == null) return null
        return JSONArray(list).toString()
    }
}
