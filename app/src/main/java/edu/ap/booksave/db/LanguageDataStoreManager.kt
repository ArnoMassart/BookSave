package edu.ap.booksave.db

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.languageDataStore by preferencesDataStore(name = "language_settings")

object LanguageDataStoreManager {
    private val LANGUAGE_KEY = stringPreferencesKey("language_key")

    fun getLanguage(context: Context): Flow<String> {
        return context.languageDataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY] ?: "en" // Default to English
        }
    }

    suspend fun setLanguage(context: Context, languageCode: String) {
        context.languageDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }
}
