package edu.ap.booksave.pages

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.booksave.components.book.BookItemCard
import edu.ap.booksave.components.BottomNavBar
import edu.ap.booksave.components.FavoriteAuthorChip
import edu.ap.booksave.components.bottomNavItems
import edu.ap.booksave.db.BookListDataStoreManager
import edu.ap.booksave.db.FavoriteAuthorsDataStoreManager
import edu.ap.booksave.models.book.Book
import edu.ap.booksave.pages.ui.theme.BookSaveTheme
import edu.ap.booksave.pages.ui.theme.LightestGray
import edu.ap.booksave.pages.ui.theme.White
import edu.ap.booksave.pages.ui.theme.titleStyle
import edu.ap.booksave.viewModels.BooksViewModel
import edu.ap.rentify.system.SystemFunctions
import kotlinx.coroutines.launch


class SearchPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SystemFunctions.hideSystemBars(window)


        setContent {
            BookSaveTheme {
                SearchScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: BooksViewModel = viewModel()) {
    val context = LocalContext.current

    var searchQueryText by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val favoriteAuthorsFlow = remember { FavoriteAuthorsDataStoreManager.favoriteAuthorsFlow(context) }
    val favoriteAuthors by favoriteAuthorsFlow.collectAsState(initial = emptySet())

    val bookListFlow = remember { BookListDataStoreManager.bookListFlow(context) }
    val savedBooks by BookListDataStoreManager.bookListFlow(context).collectAsState(initial = emptySet())

    val books = viewModel.books
    val isLoading = viewModel.isLoading

    fun filterSearch(query: String) {
        viewModel.searchBooks(query)
    }

    LaunchedEffect(Unit) {
        viewModel.loadBooks()
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                context,
                bottomNavItems.indexOfFirst { it.title == "Search" }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(top = 50.dp, start = 10.dp, end = 10.dp)
        ) {

            // Book Search Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        query = searchQueryText,
                        onQueryChange = { searchQueryText = it },
                        onSearch = {
                            if (searchQueryText.isNotBlank()) {
                                filterSearch(searchQueryText)
                            }
                            searchActive = false // ✅ Collapse search but keep screen alive
                        },
                        active = searchActive,
                        onActiveChange = {  },
                        placeholder = { Text("Search books") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = LightestGray)
                        },
                        colors = SearchBarDefaults.colors(containerColor = White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {}
                }
            }

            // Favorite authors list
            item {

                    LazyRow {
                        if(favoriteAuthors.isNotEmpty()) {
                            items(favoriteAuthors.toList()) { author ->
                                FavoriteAuthorChip(
                                    author = author,
                                    onClick = {
                                        searchQueryText = author
                                        filterSearch(author)
                                    },
                                    onRemove = {
                                        coroutineScope.launch {
                                            FavoriteAuthorsDataStoreManager.removeAuthor(context, author)
                                        }
                                    },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                        item {
                            Button(
                                onClick = {
                                    if (searchQueryText.isNotBlank()) {
                                        coroutineScope.launch {
                                            FavoriteAuthorsDataStoreManager.addAuthor(context, searchQueryText.trim())
                                        }
                                    }
                                }
                            ) {
                                Text("Add")
                            }
                        }

                }
            }

            // Show spinner while loading
            if (isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text("Getting books...")
                    }
                }
            }

            if (!isLoading) {
                if (books.isEmpty()) {
                    // Book list
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 50.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No books found!", style = titleStyle)
                        }
                    }
                } else {
                    savedBooks.forEach { it.read = false }
                    items(books) { book ->
                        Log.i("BookTest", book.toString())
                        BookItemCard(
                            book = book,
                            isAdded = savedBooks.contains(book),
                            onAddToBookList = {
                                coroutineScope.launch {
                                    BookListDataStoreManager.addBook(context, book)
                                }
                            },
                            onRemoveBookFromList = {
                                coroutineScope.launch {
                                    BookListDataStoreManager.removeBook(context, book)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}