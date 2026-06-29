package edu.ap.booksave.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import edu.ap.booksave.R
import edu.ap.booksave.components.BottomNavBar
import edu.ap.booksave.components.book.BookPageItem
import edu.ap.booksave.components.getBottomNavItems
import edu.ap.booksave.db.BookListDataStoreManager
import edu.ap.booksave.models.book.Book
import edu.ap.booksave.pages.ui.theme.BookSaveTheme
import edu.ap.booksave.pages.ui.theme.titleStyle
import edu.ap.booksave.system.SystemFunctions
import kotlinx.coroutines.launch

class BookPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SystemFunctions.hideSystemBars(window)

        setContent {
            BookSaveTheme {
                BookScreen()
            }
        }
    }
}

@Composable
fun BookScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val books by BookListDataStoreManager.bookListFlow(context).collectAsState(initial = emptySet())

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(stringResource(R.string.tab_unread), stringResource(R.string.tab_read))

    var showDeleteDialog by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }

    if (showDeleteDialog && bookToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                bookToDelete = null
            },
            title = { Text(stringResource(R.string.dialog_remove_title)) },
            text = { Text(stringResource(R.string.dialog_remove_message, bookToDelete?.title ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        bookToDelete?.let { book ->
                            coroutineScope.launch {
                                BookListDataStoreManager.removeBook(context, book)
                            }
                        }
                        showDeleteDialog = false
                        bookToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.dialog_remove_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    bookToDelete = null
                }) {
                    Text(stringResource(R.string.dialog_remove_cancel))
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                context,
                getBottomNavItems().indexOfFirst { it.route == "books" }
            )
        }
    ) { paddingValues ->

        Column(Modifier.padding(paddingValues).padding(horizontal = 10.dp, vertical = 20.dp)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val filteredBooks = when (selectedTabIndex) {
                0 -> books.filter { !it.read }
                1 -> books.filter { it.read }
                else -> books
            }

            if (filteredBooks.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty_book_list),
                    style = titleStyle,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn {
                    items(filteredBooks.toList()) { book ->
                        BookPageItem(
                            book = book,
                            onRemoveBook = {
                                bookToDelete = book
                                showDeleteDialog = true
                            },
                            onToggleRead = {
                                coroutineScope.launch {
                                    val updatedBook = book.copy(read = !book.read)
                                    BookListDataStoreManager.updateBook(context, updatedBook)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
