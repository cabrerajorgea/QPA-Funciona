package com.tramis.qpa.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tramis.qpa.utils.CameraMoveRequest
import com.tramis.qpa.utils.SimpleMapWithSalas
import com.tramis.qpa.viewmodel.SharedViewModel
import com.google.android.gms.maps.model.LatLng



@Composable
fun MapaSalasScreen(
    salas: List<Pair<String, Map<String, Any>>>,
    cameraMoveRequest: CameraMoveRequest?,
    selectedSalaId: String?,
    selectedSalaData: Map<String, Any>?,
    tarjetaVisible: Boolean,
    onCerrarTarjeta: () -> Unit,
    onSalaSeleccionada: (String, Map<String, Any>) -> Unit,
    onEntrar: (String, Map<String, Any>) -> Unit,
    sharedViewModel: SharedViewModel,
    userLocation: LatLng?
) {
    var grupoSeleccionado by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        SimpleMapWithSalas(
            salas = salas,
            cameraMoveRequest = cameraMoveRequest,
            onMarkerClick = { id, data -> onSalaSeleccionada(id, data) },
            onGrupoClick = { _, grupo ->
                grupoSeleccionado = grupo
            },
            userLocation = userLocation
        )

        // Fondo para cerrar tarjeta
        if (tarjetaVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onCerrarTarjeta() }
            )
        }

        // Tarjeta flotante
        if (tarjetaVisible && selectedSalaId != null && selectedSalaData != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Sala: ${selectedSalaData["name"] ?: "Sin nombre"}", style = MaterialTheme.typography.titleMedium)
                        Text("Usuarios: ${(selectedSalaData["usuarios"] as? List<*>)?.size ?: 0}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = {
                                sharedViewModel.agregarSala(selectedSalaId, selectedSalaData)
                                onEntrar(selectedSalaId, selectedSalaData)
                            }) {
                                Text("Entrar")
                            }
                        }
                    }
                }
            }
        }

        // Diálogo para mostrar lista de salas agrupadas
        if (grupoSeleccionado != null) {
            AlertDialog(
                onDismissRequest = { grupoSeleccionado = null },
                title = { Text("Salas en esta ubicación") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        grupoSeleccionado!!.forEach { (id, data) ->
                            val nombre = data["name"] as? String ?: "Sin nombre"
                            val cant = (data["usuarios"] as? List<*>)?.size ?: 0
                            Button(
                                onClick = {
                                    onSalaSeleccionada(id, data)
                                    grupoSeleccionado = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🎯 $nombre · $cant usuarios")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { grupoSeleccionado = null }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
}

