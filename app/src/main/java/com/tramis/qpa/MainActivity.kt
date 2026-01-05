package com.tramis.qpa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Forum
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.tramis.qpa.screens.ChatScreen
import com.tramis.qpa.screens.CrearNuevaSalaScreen
import com.tramis.qpa.screens.EditarSalaScreen
import com.tramis.qpa.screens.HistorialChatsScreen
import com.tramis.qpa.screens.HomeScreenQPA
import com.tramis.qpa.screens.ProfileScreen
import com.tramis.qpa.screens.LoginScreen
import com.tramis.qpa.ui.theme.QPATheme
import com.tramis.qpa.viewmodel.SharedViewModel
import com.tramis.qpa.viewmodel.SessionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permitir que el contenido use toda el área de la pantalla, incluso detrás de la status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        setContent {
            QPATheme {
                val navController = rememberNavController()
                val sessionViewModel: SessionViewModel = viewModel()
                val currentUser by sessionViewModel.currentUser.collectAsState()

                if (currentUser == null) {
                    LoginScreen(
                        navController = navController,
                        onSignInSuccess = {
                            // navigation to home is handled by switching the content since we are not using a navhost for login/home switch at root level in this version?
                            // Wait, looking at the AppScaffold logic, it acts as the "Home" authenticated structure.
                            // The original bad file had conflicting logic: one had a root NavHost with "login" and "home", 
                            // the other (AppScaffold) assumed it IS the structure.
                            
                            // Let's look at AppScaffold again. It has a bottom bar.
                            // Bottom bar apps usually shouldn't show the bottom bar on Login.
                            
                            // A common pattern is: 
                            // RootNavHost -> Login
                            //             -> AppScaffold (which has its own inner NavHost for tabs)
                            
                            // OR, the AppScaffold calculates visibility.
                        }
                    )
                    // The "duplicate" code had a "Root" NavHost logic.
                    // Let's implement that Root logic to wrap AppScaffold.
                    
                    NavHost(
                        navController = navController,
                        startDestination = if (currentUser != null) "app_scaffold" else "login"
                    ) {
                        composable("login") {
                            LoginScreen(
                                navController = navController,
                                onSignInSuccess = {
                                    navController.navigate("app_scaffold") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable("app_scaffold") {
                             // We need a NEW navController for inside the scaffold if we want independent back stack, 
                             // OR we use the same one but that gets messy with bottom tabs.
                             // The AppScaffold takes a navController. 
                             // If we pass the root one, the bottom bar navigation might conflict with root navigation.
                             
                             // Let's stick effectively to what the "Duplicate" code was trying to do in the first block, 
                             // but properly integrated.
                             
                             val contentNavController = rememberNavController()
                             AppScaffold(contentNavController)
                        }
                    }
                } else {
                    // Optimized: If user is logged in, show AppScaffold directly? 
                    // No, we need navigation to handle potential logout.
                    // But for now let's use the layout that seemed most "advanced" in the file which was AppScaffold.
                    
                    // Actually, looking at lines 63-113 of the original file, it had a simpler NavHost without bottom bar integration for all screens.
                    // But lines 127-231 (AppScaffold) defined a rich bottom bar.
                    // The AppScaffold is definitely the intended UI.
                    
                    // Let's construct a Main that toggles between Login and App.
                    
                   RootNavigation()
                }
            }
        }
    }
}

@Composable
fun RootNavigation() {
    val navController = rememberNavController()
    val sessionViewModel: SessionViewModel = viewModel()
    val currentUser by sessionViewModel.currentUser.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = if (currentUser != null) "main" else "login"
    ) {
        composable("login") {
            LoginScreen(
                navController = navController,
                onSignInSuccess = {
                     navController.navigate("main") {
                         popUpTo("login") { inclusive = true }
                     }
                }
            )
        }
        composable("main") {
            // Inside "main", we want the bottom bar navigation.
            // We should create a separate NavHostController for the inner navigation
            // so specifically tab switching doesn't mess with the root "login/logout" history.
            val homeNavController = rememberNavController()
            AppScaffold(homeNavController, onSignOut = {
                 sessionViewModel.signOut()
                 navController.navigate("login") {
                     popUpTo("main") { inclusive = true }
                 }
            })
        }
    }
}

@Composable
fun AppScaffold(navController: NavHostController, onSignOut: () -> Unit) {
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
                    val animatedScale by animateFloatAsState(if (isSelected) 1.3f else 1f, label = "scale")
                    val animatedColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF757575),
                        label = "color"
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
                    onSignOut = onSignOut,
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