package edu.ap.booksave.components.book

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import edu.ap.booksave.models.book.Book

@Composable
fun BookItemCard(book: Book,
                 isAdded: Boolean,
                 onAddToBookList: (Book) -> Unit,
                 onRemoveBookFromList: (Book) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = Color.Black,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Show image if it exists
            book.thumbnailUrl?.let { url ->
                Image(
                    painter = rememberAsyncImagePainter(model = url),
                    contentDescription = "Book Cover",
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 12.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title ?: "No title",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Authors: ${book.authors?.joinToString(", ") ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (isAdded) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onRemoveBookFromList(book) }) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Added to BookList",
                            tint = Color.Green,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(text = "Added to BookList")
                    }
                }
            }

            if (!isAdded) {
                IconButton(onClick = { onAddToBookList(book) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add to BookList"
                    )
                }
            }
        }
    }
}


