package com.example.infinitebook.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "book_generation_state")

/**
 * DataStore persistence manager for Book Generation continuation.
 * Preserves lastChapterIndex, lastSectionIndex, and registryJson so interrupted
 * generation seamlessly resumes from the exact position and never restarts from Chapter 1.
 */
class BookDataStore(private val context: Context) {

    suspend fun saveContinuationState(
        bookId: Long,
        lastChapterIndex: Int,
        lastSectionIndex: Int,
        registryJson: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[intPreferencesKey("last_chapter_$bookId")] = lastChapterIndex
            prefs[intPreferencesKey("last_section_$bookId")] = lastSectionIndex
            prefs[stringPreferencesKey("registry_json_$bookId")] = registryJson
        }
    }

    suspend fun getContinuationState(bookId: Long): Triple<Int, Int, String> {
        val prefs = context.dataStore.data.first()
        val chapter = prefs[intPreferencesKey("last_chapter_$bookId")] ?: 1
        val section = prefs[intPreferencesKey("last_section_$bookId")] ?: 0
        val registryJson = prefs[stringPreferencesKey("registry_json_$bookId")] ?: ""
        return Triple(chapter, section, registryJson)
    }

    suspend fun clearContinuationState(bookId: Long) {
        context.dataStore.edit { prefs ->
            prefs.remove(intPreferencesKey("last_chapter_$bookId"))
            prefs.remove(intPreferencesKey("last_section_$bookId"))
            prefs.remove(stringPreferencesKey("registry_json_$bookId"))
        }
    }
}
