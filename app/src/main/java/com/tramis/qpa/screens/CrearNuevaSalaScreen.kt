
package com.tramis.qpa.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import com.tramis.qpa.utils.crearSala
import com.tramis.qpa.utils.getCurrentLocation

@Composable
fun CrearNuevaSalaScreen(
    user: FirebaseUser?,
    onSalaSeleccionada: (String, Map<String, Any>) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var nombreSala by remember { mutableStateOf("") }
    var descripcionSala by remember { mutableStateOf("") }
    var soloLocales by remember { mutableStateOf(true) }
    var soloVisibles by remember { mutableStateOf(true) }
    var totem by remember { mutableStateOf("📍") }

    val ubicacionActual = getCurrentLocation()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Crear nueva sala", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = nombreSala,
            onValueChange = { nombreSala = it },
            label = { Text("Nombre o tema de la sala") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
        )

        OutlinedTextField(
            value = descripcionSala,
            onValueChange = { descripcionSala = it },
            label = { Text("Breve descripción de lo que se habla acá") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            minLines = 2
        )

        OutlinedTextField(
            value = totem,
            onValueChange = { totem = it },
            label = { Text("Emoji o símbolo de la sala") },
            supportingText = { Text("Solo 1 emoji") },
            singleLine = true,
            modifier = Modifier.width(80.dp)
        )

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = soloLocales, onCheckedChange = { soloLocales = it })
            Text("Solo los que estamos acá")
        }

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = soloVisibles, onCheckedChange = { soloVisibles = it })
            Text("Los que no están acá pueden leer")
        }

        Button(
            onClick = {
                val loc = ubicacionActual
                if (loc != null && user != null) {
                    crearSala(
                        nombre = nombreSala,
                        lat = loc.latitude,
                        lon = loc.longitude,
                        creatorId = user.uid,
                        soloLocales = soloLocales,
                        soloVisibles = soloVisibles,
                        totem = totem,
                        onSuccess = { id, data -> onSalaSeleccionada(id, data) },
                        onError = { e ->
                            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    Toast.makeText(context, "Ubicación o usuario no disponible", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear sala")
        }
    }
}
