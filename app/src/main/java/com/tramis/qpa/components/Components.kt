
package com.tramis.qpa.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.MapProperties
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.tramis.qpa.utils.createEmojiBitmap
import com.tramis.qpa.utils.rememberDayNightMapProperties

@Composable
fun SimpleMapWithUsuarios(
    center: LatLng,
    puntos: List<LatLng>,
    modifier: Modifier = Modifier,
    userLocation: LatLng? = null
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, 16f)
    }

    val context = LocalContext.current
    val mapProperties = rememberDayNightMapProperties()

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = mapProperties
    ) {
        userLocation?.let {
            val icon = remember { createEmojiBitmap(context, "🦉") }
            Marker(state = MarkerState(position = it), icon = icon)
        }
        puntos.forEach {
            Marker(state = MarkerState(position = it))
        }
    }
}

@Composable
fun ListaDeMensajes(
    mensajes: List<Map<String, Any>>,
    modifier: Modifier = Modifier
) {
    val usuarioActual = FirebaseAuth.getInstance().currentUser?.uid
    Column(modifier = modifier.padding(8.dp)) {
        mensajes.forEach { msg ->
            val autor = msg["senderName"] as? String ?: "Anon"
            val texto = msg["text"] as? String ?: ""
            val autorId = msg["senderId"] as? String
            val esMio = autorId == usuarioActual

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (esMio) Arrangement.End else Arrangement.Start
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = if (esMio) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(10.dp)
                        .widthIn(max = 240.dp)
                ) {
                    Text(text = autor, style = MaterialTheme.typography.labelMedium)
                    Text(text = texto)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun ListaDeSalas(
    salas: List<Pair<String, Map<String, Any>>>,
    onSalaClick: (String, Map<String, Any>) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(salas) { (id, data) ->
            val nombre = data["name"] as? String ?: "Sin nombre"
            val creador = data["creatorId"] as? String ?: "Desconocido"
            val cantUsuarios = (data["usuarios"] as? List<*>)?.size ?: 0
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.LightGray)
                    .clickable { onSalaClick(id, data) }
                    .padding(8.dp)
            ) {
                Text(text = "$nombre · $cantUsuarios usuario(s)")
                Text(text = "Creador: $creador")
            }
        }
    }
}
