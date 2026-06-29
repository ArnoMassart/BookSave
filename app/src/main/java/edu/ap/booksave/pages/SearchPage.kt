package edu.ap.booksave.pages

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.booksave.R
import edu.ap.booksave.components.book.BookItemCard
import edu.ap.booksave.components.BottomNavBar
import edu.ap.booksave.components.FavoriteAuthorChip
import edu.ap.booksave.components.getBottomNavItems
import edu.ap.booksave.db.BookListDataStoreManager
import edu.ap.booksave.db.FavoriteAuthorsDataStoreManager
import edu.ap.booksave.models.book.Book
import edu.ap.booksave.pages.ui.theme.BookSaveTheme
import edu.ap.booksave.pages.ui.theme.LightestGray
import edu.ap.booksave.pages.ui.theme.White
import edu.ap.booksave.pages.ui.theme.titleStyle
import edu.ap.booksave.viewModels.BooksViewModel
import edu.ap.booksave.system.SystemFunctions
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class SearchPage : AppCompatActivity() {
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

    val savedBooks by BookListDataStoreManager.bookListFlow(context).collectAsState(initial = emptySet())

    val books = viewModel.books
    val isLoading = viewModel.isLoading
    val totalItems = viewModel.totalItems
    val currentPage = viewModel.currentPage

    var showDeleteDialog by remember { mutableStateOf(false) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }

    fun filterSearch(query: String) {
        viewModel.searchBooks(query)
    }

    LaunchedEffect(Unit) {
        viewModel.loadBooks()
    }

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
                getBottomNavItems().indexOfFirst { it.route == "search" }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 20.dp, start = 10.dp, end = 10.dp)
            ) {
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
                            searchActive = false
                        },
                        active = searchActive,
                        onActiveChange = { },
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = LightestGray)
                        },
                        colors = SearchBarDefaults.colors(containerColor = White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {}
                }

                LazyRow(modifier = Modifier.padding(bottom = 10.dp)) {
                    if (favoriteAuthors.isNotEmpty()) {
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
                            Text(stringResource(R.string.add_author))
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.loading_books))
                    }
                } else if (books.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(stringResource(R.string.no_books_found), style = titleStyle)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp)
                    ) {
                        items(books) { book ->
                            BookItemCard(
                                book = book,
                                isAdded = savedBooks.any { it.title == book.title },
                                onAddToBookList = {
                                    coroutineScope.launch {
                                        BookListDataStoreManager.addBook(context, book)
                                    }
                                },
                                onRemoveBookFromList = {
                                    bookToDelete = book
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            PaginationControls(
                currentPage = currentPage,
                totalItems = totalItems,
                pageSize = viewModel.pageSize,
                onPageSelected = { viewModel.goToPage(it) }
            )
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalItems: Int,
    pageSize: Int,
    onPageSelected: (Int) -> Unit
) {
    val totalPages = max(1, ceil(totalItems.toDouble() / pageSize).toInt())

    val visiblePages = 5
    val startPage = max(1, min(currentPage - visiblePages / 2, max(1, totalPages - visiblePages + 1)))
    val endPage = min(totalPages, startPage + visiblePages - 1)

    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        color = White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (startPage > 1) {
                PageBox(page = 1, isSelected = false, onClick = { onPageSelected(1) })
                if (startPage > 2) Text("...", modifier = Modifier.padding(horizontal = 4.dp))
            }

            for (i in startPage..endPage) {
                PageBox(
                    page = i,
                    isSelected = i == currentPage,
                    onClick = { onPageSelected(i) }
                )
            }

            if (endPage < totalPages) {
                if (endPage < totalPages - 1) Text("...", modifier = Modifier.padding(horizontal = 4.dp))
                PageBox(page = totalPages, isSelected = false, onClick = { onPageSelected(totalPages) })
            }
        }
    }
}

@Composable
fun PageBox(page: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(36.dp)
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF6DA3A2) else Color.LightGray,
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = if (isSelected) Color(0xFF6DA3A2) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = page.toString(),
            color = if (isSelected) Color.White else Color.Black,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
