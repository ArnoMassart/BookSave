package edu.ap.booksave.components

import android.content.Context
import android.content.Intent
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import edu.ap.booksave.R
import edu.ap.booksave.models.NavItem
import edu.ap.booksave.pages.BookPage
import edu.ap.booksave.pages.SearchPage
import edu.ap.booksave.pages.SettingsPage
import edu.ap.booksave.pages.ui.theme.White

@Composable
fun getBottomNavItems() = listOf(
    NavItem(stringResource(R.string.nav_books), Icons.Filled.Book, Icons.Outlined.Book, "books"),
    NavItem(stringResource(R.string.nav_search), Icons.Filled.Search, Icons.Outlined.Search, "search"),
    NavItem(stringResource(R.string.nav_settings), Icons.Filled.Settings, Icons.Outlined.Settings, "settings")
)

@Composable
fun BottomNavBar(context: Context, selectedItem: Int) {
    val items = getBottomNavItems()
    NavigationBar(containerColor = White) {
        items.forEachIndexed { index, item ->
            val icon = if (index == selectedItem) item.selectedIcon else item.unselectedIcon
            val iconTint = if (index == selectedItem) Color(0xFF6DA3A2) else Color(0xFFBFBFBF)

            NavigationBarItem(
                selected = selectedItem == index,
                onClick = {
                    val intent = when (item.route) {
                        "books" -> Intent(context, BookPage::class.java)
                        "search"-> Intent(context, SearchPage::class.java)
                        "settings" -> Intent(context, SettingsPage::class.java)
                        else -> null
                    }
                    intent?.let { if (index != selectedItem) context.startActivity(it) }
                },
                icon = { Icon(icon, contentDescription = item.title, tint = iconTint)},
                label = { Text(item.title, color = iconTint)},
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
