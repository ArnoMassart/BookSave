package edu.ap.booksave.components.book

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import edu.ap.booksave.R
import edu.ap.booksave.models.book.Book
import edu.ap.booksave.pages.ui.theme.LightestGray

@Composable
fun BookPageItem(book: Book, onRemoveBook: (Book) -> Unit,    onToggleRead: () -> Unit
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
                    text = book.title ?: stringResource(R.string.book_no_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.book_authors, book.authors?.joinToString(", ") ?: stringResource(R.string.unknown)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
            Column {
                IconButton(
                    onClick = { onRemoveBook(book) }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = LightestGray)
                }
                Spacer(modifier = Modifier.height(2.dp))
                IconButton(onClick = onToggleRead) {
                    Icon(
                        imageVector = if (book.read) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (book.read) stringResource(R.string.book_mark_unread) else stringResource(R.string.book_mark_read)
                    )
                }
            }
        }
    }
}
