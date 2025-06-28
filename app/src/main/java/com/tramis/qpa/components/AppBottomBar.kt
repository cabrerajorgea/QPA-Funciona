
package com.tramis.qpa.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@Composable
fun AppBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar {
        val items = listOf(
            BottomNavItem("Explorar", Icons.Default.Search, 0),
            BottomNavItem("Crear", Icons.Default.Add, 1),
            BottomNavItem("Chats", Icons.Default.Message, 2),
            BottomNavItem("Perfil", Icons.Default.Person, 3)
        )
        
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedTab == item.index,
                onClick = { onTabSelected(item.index) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)
