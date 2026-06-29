package edu.ap.booksave.pages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import edu.ap.booksave.components.BottomNavBar
import edu.ap.booksave.components.book.BookPageItem
import edu.ap.booksave.components.bottomNavItems
import edu.ap.booksave.db.BookListDataStoreManager
import edu.ap.booksave.models.book.Book
import edu.ap.booksave.pages.ui.theme.BookSaveTheme
import edu.ap.booksave.pages.ui.theme.titleStyle
import edu.ap.rentify.system.SystemFunctions
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.google.gson.reflect.TypeToken


class BookPage : ComponentActivity() {
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
    val tabs = listOf("Unread", "Read")


    Scaffold(
        bottomBar = {
            BottomNavBar(
                context,
                bottomNavItems.indexOfFirst { it.title == "Books" }
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
                    text = "No books in this list.",
                    style = titleStyle,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn {
                    items(filteredBooks.toList()) { book ->
                        BookPageItem(
                            book = book,
                            onRemoveBook = {
                                coroutineScope.launch {
                                    BookListDataStoreManager.removeBook(context, book)
                                }
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