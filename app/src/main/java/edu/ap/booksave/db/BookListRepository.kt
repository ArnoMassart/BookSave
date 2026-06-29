package edu.ap.booksave.db

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.ap.booksave.models.book.Book
import kotlinx.coroutines.flow.map

val Context.bookListDataStore by preferencesDataStore(name = "book_list")

object BookListDataStoreManager {
    val gson = Gson()

    private val BOOK_LIST_KEY = PreferencesKeys.stringSetKey("book_list_key")

    // Extension property for PreferencesKeys.stringSetKey
    private object PreferencesKeys {
        fun stringSetKey(name: String) = androidx.datastore.preferences.core.stringSetPreferencesKey(name)
    }

    suspend fun addBook(context: Context, book: Book) {
        context.bookListDataStore.edit { preferences ->
            val currentJsonSet = preferences[BOOK_LIST_KEY] ?: emptySet()
            val bookJson = gson.toJson(book)
            preferences[BOOK_LIST_KEY] = currentJsonSet + bookJson
        }
    }

    suspend fun removeBook(context: Context, book: Book) {
        context.bookListDataStore.edit { preferences ->
            val currentJsonSet = preferences[BOOK_LIST_KEY] ?: emptySet()
            val bookJson = gson.toJson(book)
            preferences[BOOK_LIST_KEY] = currentJsonSet - bookJson
        }
    }

    suspend fun clearBookList(context: Context) {
        context.bookListDataStore.edit { preferences ->
            preferences.clear() // clears all keys/values in this DataStore file
        }
    }

    suspend fun replaceBookList(context: Context, books: List<Book>) {
        context.bookListDataStore.edit { preferences ->
            val jsonSet = books.map { gson.toJson(it) }.toSet()
            preferences[BOOK_LIST_KEY] = jsonSet
        }
    }

    suspend fun markBookAsRead(context: Context, book: Book) {
        val updatedBook = book.copy(read = true)
        updateBook(context, updatedBook)
    }

    suspend fun markBookAsUnread(context: Context, book: Book) {
        val updatedBook = book.copy(read = false)
        updateBook(context, updatedBook)
    }

    // Helper to update the book in storage by removing old and adding new
    suspend fun updateBook(context: Context, updatedBook: Book) {
        context.bookListDataStore.edit { preferences ->
            val currentJsonSet = preferences[BOOK_LIST_KEY] ?: emptySet()
            val gson = Gson()
            val oldJson = currentJsonSet.firstOrNull { json ->
                val book = gson.fromJson(json, Book::class.java)
                book.title == updatedBook.title // match by title or any unique ID you have
            }
            val newSet = currentJsonSet - (oldJson ?: "") + gson.toJson(updatedBook)
            preferences[BOOK_LIST_KEY] = newSet
        }
    }

    val bookListFlow = { context: Context ->
        context.bookListDataStore.data.map { preferences ->
            val jsonSet = preferences[BOOK_LIST_KEY] ?: emptySet()
            val type = object : TypeToken<Book>() {}.type
            jsonSet.map { gson.fromJson<Book>(it, type) }
        }
    }
}
