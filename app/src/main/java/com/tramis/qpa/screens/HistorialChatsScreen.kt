package com.tramis.qpa.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.tramis.qpa.viewmodel.SharedViewModel

@Composable
fun HistorialChatsScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel
) {
    val historial by sharedViewModel.salasAccedidas.collectAsState()
    val salasCreadas by sharedViewModel.salasCreadas.collectAsState()
    var textoBusqueda by remember { mutableStateOf("") }
    var activas by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedTab by remember { mutableStateOf(0) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val historialFiltrado = historial.filter { (id, data) ->
        data["creatorId"] != userId
    }

    // Cargar las salas activas y las creadas
    LaunchedEffect(Unit) {
        sharedViewModel.actualizarSalasActivas {
            activas = it
        }
        userId?.let { uid ->
            sharedViewModel.cargarSalasCreadas(uid)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val tabTitles = listOf("Creadas", "Accedidas")
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            label = { Text("Buscar sala") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        val listaActual = if (selectedTab == 0) salasCreadas else historialFiltrado

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listaActual.filter {
                val nombre = it.second["name"] as? String ?: ""
                nombre.contains(textoBusqueda, ignoreCase = true)
            }) { (id, data) ->
                val nombre = data["name"] as? String ?: "(Sin nombre)"
                val creador = data["creatorId"] as? String ?: "Desconocido"
                val usuarios = (data["usuarios"] as? List<*>)?.size ?: 0
                val activa = activas.contains(id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(if (activa) Color.White else Color.LightGray)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🎯 $nombre", style = MaterialTheme.typography.titleMedium)
                        Text("👥 $usuarios usuarios | 🧑 $creador")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    navController.navigate("chat/$id")
                                },
                                enabled = activa
                            ) {
                                Text("Entrar")
                            }
                        }
                    }
                }
            }
        }
    }
}
