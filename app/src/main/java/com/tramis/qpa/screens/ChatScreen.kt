package com.tramis.qpa.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.tramis.qpa.components.*
import com.tramis.qpa.utils.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tramis.qpa.viewmodel.SharedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    salaId: String,
    navController: NavHostController
) {
    val mensajes = useMensajesListener(salaId)
    val usuarios = useUsuariosEnSalaListener(salaId)
    val sala = useSalaInfo(salaId)

    val usuario = FirebaseAuth.getInstance().currentUser
    var mensaje by remember { mutableStateOf(TextFieldValue("")) }

    val centroSala = (sala?.get("location") as? GeoPoint)?.let {
        LatLng(it.latitude, it.longitude)
    } ?: LatLng(0.0, 0.0)

    val sharedViewModel: SharedViewModel = viewModel()
    var salaRegistrada by remember { mutableStateOf(false) }

    LaunchedEffect(sala) {
        if (!salaRegistrada && sala != null) {
            sharedViewModel.agregarSala(salaId, sala)
            salaRegistrada = true
        }
    }

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
                .alpha(0.1f)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            TopAppBar(
                title = {
                    Text(text = sala?.get("name") as? String ?: "Sala")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("chatList")
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")

                    }
                }
            )

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
                            onError = {}
                        )
                    }
                }) {
                    Text("Enviar")
                }
            }
        }
    }
}
