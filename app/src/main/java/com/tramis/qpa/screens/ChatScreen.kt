package com.tramis.qpa.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.tramis.qpa.components.*
import com.tramis.qpa.utils.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.tramis.qpa.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    salaId: String,
    navController: NavHostController,
    sharedViewModel: SharedViewModel
) {
    val mensajes = useMensajesListener(salaId)
    val usuarios = useUsuariosEnSalaListener(salaId)
    val sala = useSalaInfo(salaId)
    val ubicacionActual = getCurrentLocation()

    val usuario = FirebaseAuth.getInstance().currentUser
    var mensaje by remember { mutableStateOf(TextFieldValue("")) }

    var salaRegistrada by remember { mutableStateOf(false) }

    LaunchedEffect(sala) {
        if (!salaRegistrada && sala != null) {
            sharedViewModel.agregarSala(salaId, sala)
            salaRegistrada = true
        }
    }

    if (sala == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val geo = sala["location"] as? GeoPoint
    val centroSala = geo?.let { LatLng(it.latitude, it.longitude) } ?: LatLng(0.0, 0.0)
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa de fondo atenuado
        SimpleMapWithUsuarios(
            center = centroSala,
            puntos = usuarios.mapNotNull {
                val loc = it["location"] as? GeoPoint
                loc?.let { LatLng(it.latitude, it.longitude) }
            },
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.1f),
            userLocation = ubicacionActual
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Encabezado personalizado sin TopAppBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = {
                    navController.navigateUp()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = sala["totem"] as? String ?: "",
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = sala["name"] as? String ?: "Sala",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            // Marquesina de actividad de sala
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Room activity will appear here",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Lista de mensajes
            ListaDeMensajes(
                mensajes = mensajes,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            // Campo de entrada
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = mensaje,
                    onValueChange = { mensaje = it },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, shape = MaterialTheme.shapes.small)
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (!mensaje.text.isBlank()) {
                        enviarMensaje(
                            roomId = salaId,
                            senderId = usuario?.uid ?: "anon",
                            senderName = usuario?.displayName ?: "Anónimo",
                            texto = mensaje.text,
                            onSuccess = { mensaje = TextFieldValue("") },
                            onError = { e ->
                                Toast.makeText(context, "Error al enviar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                }) {
                    Text("Enviar")
                }
            }
        }
    }
}
