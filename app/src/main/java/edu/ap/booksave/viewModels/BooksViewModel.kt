package edu.ap.booksave.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.booksave.instances.RetrofitInstance
import edu.ap.booksave.models.book.Book
import edu.ap.booksave.models.book.BookItem
import kotlinx.coroutines.launch

class BooksViewModel: ViewModel() {
    var books by mutableStateOf<List<Book>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun searchBooks(query: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val result = RetrofitInstance.api.searchBooks(query)
                books = result.items?.map { item ->
                    Book(
                        title = item.volumeInfo.title,
                        authors = item.volumeInfo.authors,
                        description = item.volumeInfo.description,
                        thumbnailUrl = item.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
                    )
                }?.filterNot { book ->
                    val title = book.title?.lowercase() ?: ""
                    title.contains("box") || title.contains("set") || title.contains("bundel") || title.contains("collection")
                } ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                books = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    fun loadBooks() {
        viewModelScope.launch {
            isLoading = true
            try {
                val result = RetrofitInstance.api.searchBooks("e")
                books = result.items?.map { item ->
                    Book(
                        title = item.volumeInfo.title,
                        authors = item.volumeInfo.authors,
                        description = item.volumeInfo.description,
                        thumbnailUrl = item.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
                    )
                } ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
                books = emptyList()
            } finally {
                isLoading = false
            }
        }
    }
}