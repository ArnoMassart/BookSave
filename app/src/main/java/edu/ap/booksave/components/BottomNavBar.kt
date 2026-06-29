package edu.ap.booksave.components

import android.content.Context
import android.content.Intent
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import edu.ap.booksave.models.NavItem
import edu.ap.booksave.pages.BookPage
import edu.ap.booksave.pages.SearchPage
import edu.ap.booksave.pages.ui.theme.White

val bottomNavItems = listOf(
    NavItem("Books", Icons.Filled.Book, Icons.Outlined.Book, "books"),
    NavItem("Search", Icons.Filled.Search, Icons.Outlined.Search, "search")
)

@Composable
fun BottomNavBar(context: Context, selectedItem: Int) {
    NavigationBar(containerColor = White) {
        bottomNavItems.forEachIndexed { index, item ->
            val icon = if (index == selectedItem) item.selectedIcon else item.unselectedIcon
            val iconTint = if (index == selectedItem) Color(0xFF6DA3A2) else Color(0xFFBFBFBF)

            NavigationBarItem(
                selected = selectedItem == index,
                onClick = {
                    val intent = when (item.route) {
                        "books" -> Intent(context, BookPage::class.java)
                        "search"-> Intent(context, SearchPage::class.java)
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

