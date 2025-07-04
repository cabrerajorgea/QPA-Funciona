package com.tramis.qpa.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseUser
import com.tramis.qpa.utils.*
import com.tramis.qpa.viewmodel.SharedViewModel

@Composable
fun HomeScreenQPA(
    user: FirebaseUser?,
    navController: NavController,
    onSignOut: () -> Unit,
    sharedViewModel: SharedViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    var permisoOtorgado by remember { mutableStateOf(false) }

    SolicitarPermisoUbicacion {
        permisoOtorgado = true
    }

    if (!permisoOtorgado) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val ubicacionUsuario = getCurrentLocation()
    if (ubicacionUsuario == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(Unit) {
        selectedTab = 0
    }

    val salas = useSalasListener()
    var moveCounter by remember { mutableStateOf(0) }
    var cameraMoveRequest by remember {
        mutableStateOf(CameraMoveRequest(ubicacionUsuario, moveCounter))
    }

    LaunchedEffect(ubicacionUsuario) {
        moveCounter++
        cameraMoveRequest = CameraMoveRequest(ubicacionUsuario, moveCounter)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSalaId by remember { mutableStateOf<String?>(null) }
    var selectedSalaData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var tarjetaVisible by remember { mutableStateOf(false) }
    val filteredSalas = salas.filter { salaPair ->
        val nombre = salaPair.second["name"] as? String ?: ""
        nombre.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
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
                            sharedViewModel = sharedViewModel,
                            userLocation = ubicacionUsuario
                        )

                        val listState = rememberLazyListState()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.25f)
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.4f)
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text("Buscar sala") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                                    items(filteredSalas) { salaPair ->
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
                                                    val geo = data["location"] as? com.google.firebase.firestore.GeoPoint
                                                    geo?.let {
                                                        moveCounter++
                                                        cameraMoveRequest = CameraMoveRequest(
                                                            com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude),
                                                            moveCounter
                                                        )
                                                    }
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
                    }
                }

                1 -> CrearNuevaSalaScreen(
                    currentUser = user,
                    onSalaSeleccionada = { id, data ->
                        navController.navigate("chat/$id")
                    },
                    sessionViewModel = sharedViewModel,
                    sharedViewModel = sharedViewModel
                )

                2 -> HistorialChatsScreen(
                    navController = navController,
                    sharedViewModel = sharedViewModel
                )
                3 -> Text("Perfil y ajustes")
            }
        }
    }
}
