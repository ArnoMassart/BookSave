package edu.ap.booksave.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ap.booksave.instances.RetrofitInstance
import edu.ap.booksave.models.book.Book
import kotlinx.coroutines.launch
import kotlin.math.min

class BooksViewModel: ViewModel() {
    var books by mutableStateOf<List<Book>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var totalItems by mutableStateOf(0)
        private set

    var currentPage by mutableStateOf(1)
        private set

    private var currentQuery = "e"
    val pageSize = 20

    fun searchBooks(query: String, page: Int = 1) {
        currentQuery = query
        currentPage = page
        viewModelScope.launch {
            isLoading = true
            try {
                val startIndex = (page - 1) * pageSize
                val result = RetrofitInstance.api.searchBooks(query, startIndex = startIndex, maxResults = pageSize)
                
                val rawItems = result.items ?: emptyList()
                
                // Adjust totalItems based on actual results received
                if (rawItems.isEmpty()) {
                    totalItems = if (page == 1) 0 else (page - 1) * pageSize
                } else if (rawItems.size < pageSize) {
                    totalItems = (page - 1) * pageSize + rawItems.size
                } else {
                    // Capping at 400 because Google Books API becomes unstable at high offsets
                    // and totalItems is often an over-estimate.
                    totalItems = min(result.totalItems, 400)
                }

                books = rawItems.map { item ->
                    Book(
                        title = item.volumeInfo.title,
                        authors = item.volumeInfo.authors,
                        description = item.volumeInfo.description,
                        thumbnailUrl = item.volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://")
                    )
                }.filterNot { book ->
                    val title = book.title?.lowercase() ?: ""
                    title.contains("box") || title.contains("set") || title.contains("bundel") || title.contains("collection")
                }
                
                // If filtering removed all books on this page, but there might be more...
                // For now, we show "No books found" on this page if filtered list is empty.
                
            } catch (e: Exception) {
                e.printStackTrace()
                books = emptyList()
                totalItems = 0
            } finally {
                isLoading = false
            }
        }
    }

    fun loadBooks() {
        searchBooks("e", 1)
    }
    
    fun goToPage(page: Int) {
        if (page != currentPage) {
            searchBooks(currentQuery, page)
        }
    }
}
