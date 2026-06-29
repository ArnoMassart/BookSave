package edu.ap.booksave.pages

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.google.gson.reflect.TypeToken
import edu.ap.booksave.R
import edu.ap.booksave.components.BottomNavBar
import edu.ap.booksave.components.getBottomNavItems
import edu.ap.booksave.db.BookListDataStoreManager
import edu.ap.booksave.db.LanguageDataStoreManager
import edu.ap.booksave.models.book.Book
import edu.ap.booksave.pages.ui.theme.BookSaveTheme
import edu.ap.booksave.pages.ui.theme.titleStyle
import edu.ap.booksave.system.SystemFunctions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingsPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SystemFunctions.hideSystemBars(window)

        setContent {
            BookSaveTheme {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage by LanguageDataStoreManager.getLanguage(context).collectAsState(initial = "en")

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain"),
        onResult = { uri: Uri? ->
            uri?.let {
                coroutineScope.launch {
                    try {
                        val books = BookListDataStoreManager.bookListFlow(context).first()
                        val json = BookListDataStoreManager.gson.toJson(books)
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write(json.toByteArray())
                        }
                        Toast.makeText(context, context.getString(R.string.toast_export_success), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.toast_export_failed, e.message), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                coroutineScope.launch {
                    try {
                        context.contentResolver.openInputStream(it)?.use { inputStream ->
                            val reader = BufferedReader(InputStreamReader(inputStream))
                            val json = reader.readText()
                            val type = object : TypeToken<List<Book>>() {}.type
                            val books: List<Book> = BookListDataStoreManager.gson.fromJson(json, type)
                            BookListDataStoreManager.replaceBookList(context, books)
                        }
                        Toast.makeText(context, context.getString(R.string.toast_import_success), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, context.getString(R.string.toast_import_failed, e.message), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    Scaffold(
        bottomBar = {
            BottomNavBar(
                context,
                getBottomNavItems().indexOfFirst { it.route == "settings" }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(stringResource(R.string.settings_title), style = titleStyle, modifier = Modifier.padding(bottom = 32.dp))

            Button(
                onClick = { exportLauncher.launch("books_backup.txt") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.settings_export))
            }

            Button(
                onClick = { importLauncher.launch(arrayOf("text/plain")) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.settings_import))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val languages = listOf("en" to "English", "nl" to "Nederlands")
                languages.forEach { (code, name) ->
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                LanguageDataStoreManager.setLanguage(context, code)
                                val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(code)
                                AppCompatDelegate.setApplicationLocales(appLocale)
                            }
                        },
                        colors = if (currentLanguage == code) 
                            ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f).padding(8.dp)
                    ) {
                        Text(name)
                    }
                }
            }
        }
    }
}
