package com.tramis.qpa.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseUser
import com.tramis.qpa.utils.CameraMoveRequest
import com.tramis.qpa.utils.getCurrentLocation
import com.tramis.qpa.utils.useSalasListener
import com.tramis.qpa.viewmodel.SharedViewModel

@Composable
fun HomeScreenQPA(
    user: FirebaseUser?,
    navController: NavController,
    onSignOut: () -> Unit
) {
    val sharedViewModel: SharedViewModel = viewModel()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        selectedTab = 0
    }

    val salas = useSalasListener()
    val ubicacionUsuario = getCurrentLocation()
    var moveCounter by remember { mutableStateOf(0) }
    val cameraMoveRequest = remember(ubicacionUsuario) {
        ubicacionUsuario?.let {
            moveCounter++
            CameraMoveRequest(it, moveCounter)
        }
    }

    var selectedSalaId by remember { mutableStateOf<String?>(null) }
    var selectedSalaData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var tarjetaVisible by remember { mutableStateOf(false) }

    Scaffold {
        Box(modifier = Modifier.padding(it)) {
            when (selectedTab) {
                0 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            MapaSalasScreen(
                                salas = salas,
                                cameraMoveRequest = cameraMoveRequest,
                                selectedSalaId = selectedSalaId,
                                selectedSalaData = selectedSalaData,
                                tarjetaVisible = tarjetaVisible,
                                onCerrarTarjeta = {
                                    tarjetaVisible = false
                                    selectedSalaId = null
                                    selectedSalaData = null
                                },
                                onSalaSeleccionada = { id, data ->
                                    selectedSalaId = id
                                    selectedSalaData = data
                                    tarjetaVisible = true
                                },
                                onEntrar = { id, data ->
                                    sharedViewModel.agregarSala(id, data)
                                    navController.navigate("chat/$id")
                                },
                                sharedViewModel = sharedViewModel
                            )
                        }
                        HorizontalDivider()
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(8.dp)
                        ) {
                            items(salas) { salaPair ->
                                val (id, data) = salaPair
                                val nombre = data["name"] as? String ?: "Sin nombre"
                                val creador = data["creatorId"] as? String ?: "Desconocido"
                                val cantUsuarios = (data["usuarios"] as? List<*>)?.size ?: 0
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            selectedSalaId = id
                                            selectedSalaData = data
                                            tarjetaVisible = true
                                            sharedViewModel.agregarSala(id, data)
                                            navController.navigate("chat/$id")
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("🎯 $nombre")
                                        Text("👥 $cantUsuarios usuarios | 🧑 $creador")
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> CrearNuevaSalaScreen(user = user, onSalaSeleccionada = { id, data ->
                    navController.navigate("chat/$id")
                }, onSignOut = onSignOut)
                2 -> Text("Mis chats")
                3 -> Text("Perfil y ajustes")
            }
        }
    }
}
