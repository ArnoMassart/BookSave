package edu.ap.booksave.api

import edu.ap.booksave.models.book.BookResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BooksApi {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 40,
        @Query("langRestrict") language: String = "nl",
        @Query("key") apiKey: String = "API_KEY"
    ): BookResponse
}
