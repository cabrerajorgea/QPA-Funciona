package com.tramis.qpa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.tramis.qpa.screens.ChatScreen
import com.tramis.qpa.screens.CrearNuevaSalaScreen
import com.tramis.qpa.screens.HistorialChatsScreen
import com.tramis.qpa.screens.HomeScreenQPA
import com.tramis.qpa.ui.theme.QPATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QPATheme {
                val navController = rememberNavController()
                AppScaffold(navController)
            }
        }
    }
}

@Composable
fun AppScaffold(navController: NavHostController) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = currentBackStackEntry?.destination

                listOf("home" to "🏠", "crear" to "➕", "chatList" to "💬", "perfil" to "👤").forEach { (route, emoji) ->
                    NavigationBarItem(
                        selected = currentDestination?.route == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(emoji) },
                        icon = {}
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreenQPA(
                    user = null,
                    navController = navController,
                    onSignOut = {}
                )
            }
            composable("crear") {
                CrearNuevaSalaScreen(
                    user = null,
                    onSalaSeleccionada = { _, _ -> },
                    onSignOut = {}
                )
            }
            composable("chatList") {
                HistorialChatsScreen(navController = navController)
            }
            composable("chat/{salaId}") { backStackEntry ->
                val salaId = backStackEntry.arguments?.getString("salaId") ?: return@composable
                ChatScreen(
                    salaId = salaId,
                    navController = navController
                )
            }
            composable("perfil") {
                Text("Pantalla de Perfil")
            }
        }
    }
}
