package com.tramis.qpa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.tramis.qpa.screens.ChatScreen
import com.tramis.qpa.screens.CrearNuevaSalaScreen
import com.tramis.qpa.screens.HistorialChatsScreen
import com.tramis.qpa.screens.HomeScreenQPA
import com.tramis.qpa.screens.ProfileScreen
import com.tramis.qpa.screens.LoginScreen
import com.tramis.qpa.ui.theme.QPATheme
import com.tramis.qpa.viewmodel.SharedViewModel
import com.tramis.qpa.viewmodel.SessionViewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.tramis.qpa.screens.EditarSalaScreen
import androidx.compose.material.icons.rounded.Forum


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permitir que el contenido use toda el área de la pantalla, incluso detrás de la status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        setContent {

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            QPATheme {
                val navController = rememberNavController()
                val sharedViewModel: SharedViewModel = viewModel()
                val sessionViewModel: SessionViewModel = viewModel()
                val currentUser by sessionViewModel.currentUser.collectAsState()
                
                NavHost(
                    navController = navController,
                    startDestination = if (currentUser != null) "home" else "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            navController = navController,
                            onSignInSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    
                    composable("home") {
                        HomeScreenQPA(
                            user = currentUser,
                            navController = navController,
                            onSignOut = {
                                sessionViewModel.signOut()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            sharedViewModel = sharedViewModel
                        )
                    }
                    
                    composable("chat/{roomId}") { backStackEntry ->
                        val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                        ChatScreen(
                            roomId = roomId,
                            navController = navController,
                            sharedViewModel = sharedViewModel
                        )
                    }
                    
                    composable("profile") {
                        ProfileScreen(
                            navController = navController
                        )
                    }
                    
                    composable("historial") {
                        HistorialChatsScreen(
                            navController = navController,
                            sharedViewModel = sharedViewModel
                        )
                    }
                }
            }
        }
    }
}

            QPATheme {
                val navController = rememberNavController()
                AppScaffold(navController)
            }
        }
    }
}

@Composable
fun AppScaffold(navController: NavHostController) {
    val activity = LocalContext.current as ComponentActivity
    val sharedViewModel: SharedViewModel = viewModel(viewModelStoreOwner = activity)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = currentBackStackEntry?.destination

                val items = listOf(
                    "home" to Icons.Rounded.Place,
                    "crear" to Icons.Rounded.Add,
                    "chatList" to Icons.Rounded.Forum,
                    "perfil" to Icons.Rounded.AccountCircle
                )

                items.forEach { (route, icon) ->
                    val isSelected = currentDestination?.route == route
                    val animatedScale by animateFloatAsState(if (isSelected) 1.3f else 1f)
                    val animatedColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF757575)
                    )

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = route,
                                modifier = Modifier
                                    .size(48.dp)
                                    .scale(animatedScale),
                                tint = animatedColor
                            )
                        },
                        label = null,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
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
                    onSignOut = {},
                    sharedViewModel = sharedViewModel
                )
            }
            composable("crear") {
                val user = FirebaseAuth.getInstance().currentUser
                CrearNuevaSalaScreen(
                    currentUser = user,
                    onSalaSeleccionada = { id, data -> navController.navigate("chat/$id") },
                    sessionViewModel = sharedViewModel,
                    sharedViewModel = sharedViewModel
                )
            }
            composable("chatList") {
                HistorialChatsScreen(
                    navController = navController,
                    sharedViewModel = sharedViewModel
                )

            }
            composable("chat/{salaId}") { backStackEntry ->
                val salaId = backStackEntry.arguments?.getString("salaId") ?: return@composable
                ChatScreen(
                    salaId = salaId,
                    navController = navController,
                    sharedViewModel = sharedViewModel
                )
            }
            composable("perfil") {
                ProfileScreen(navController)
            }
            composable("editarSala/{salaId}") { backStackEntry ->
                val salaId = backStackEntry.arguments?.getString("salaId") ?: return@composable
                EditarSalaScreen(salaId = salaId, navController = navController)
            }
        }
    }
}