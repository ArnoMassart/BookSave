package edu.ap.booksave.db

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object FavoriteAuthorsDataStoreManager {

    private val Context.dataStore by preferencesDataStore("favorite_authors")

    private val FAVORITE_AUTHORS_KEY = stringSetPreferencesKey("favorite_authors_key")

    // Expose Flow of saved authors
    fun favoriteAuthorsFlow(context: Context): Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[FAVORITE_AUTHORS_KEY] ?: emptySet()
        }

    // Add an author
    suspend fun addAuthor(context: Context, author: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITE_AUTHORS_KEY] ?: emptySet()
            preferences[FAVORITE_AUTHORS_KEY] = current + author
        }
    }

    // Remove an author
    suspend fun removeAuthor(context: Context, author: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITE_AUTHORS_KEY] ?: emptySet()
            preferences[FAVORITE_AUTHORS_KEY] = current - author
        }
    }

    // Optional: Clear all favorite authors
    suspend fun clearAuthors(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
