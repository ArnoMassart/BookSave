package edu.ap.booksave.models.book

data class Book(
    val title: String?,
    val authors: List<String>?,
    val description: String?,
    val thumbnailUrl: String?,
    var read: Boolean = false
)
