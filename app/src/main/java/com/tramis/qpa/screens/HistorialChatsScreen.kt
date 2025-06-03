package com.tramis.qpa.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
    var textoBusqueda by remember { mutableStateOf("") }
    var activas by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Cargar las salas activas
    LaunchedEffect(Unit) {
        sharedViewModel.actualizarSalasActivas {
            activas = it
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mis salas accedidas", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            label = { Text("Buscar sala") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(historial.filter {
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
