package com.tramis.qpa.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
                val totem = data["totem"] as? String ?: "📍"

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("$totem $nombre", style = MaterialTheme.typography.titleMedium)
                        Text("👥 $usuarios usuarios | 🧑 $creador")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            if (selectedTab == 0) {
                                Button(onClick = { navController.navigate("editarSala/$id") }) {
                                    Text("Editar sala")
                                }
                                Spacer(Modifier.width(8.dp))
                            }
                            Button(
                                onClick = { navController.navigate("chat/$id") },
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
