package com.tramis.qpa.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tramis.qpa.utils.editarSala
import com.tramis.qpa.utils.useSalaInfo

@Composable
fun EditarSalaScreen(salaId: String, navController: NavHostController) {
    val sala = useSalaInfo(salaId)

    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var totem by remember { mutableStateOf("\uD83D\uDCCD") }
    var soloVisibles by remember { mutableStateOf(true) }

    LaunchedEffect(sala) {
        sala?.let {
            nombre = it["name"] as? String ?: ""
            descripcion = it["description"] as? String ?: ""
            totem = it["totem"] as? String ?: "\uD83D\uDCCD"
            soloVisibles = it["soloVisibles"] as? Boolean ?: true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Editar sala", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre de la sala") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("¿De qué se habla acá?") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        OutlinedTextField(
            value = totem,
            onValueChange = {
                if (it.codePointCount(0, it.length) <= 1) {
                    totem = it
                }
            },
            label = { Text("Totem / Emoji") },
            modifier = Modifier.width(80.dp),
            singleLine = true
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = soloVisibles,
                onCheckedChange = { soloVisibles = it }
            )
            Text("Cualquiera puede leer")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                editarSala(
                    salaId = salaId,
                    nombre = nombre,
                    soloVisibles = soloVisibles,
                    totem = totem,
                    onSuccess = { navController.popBackStack() },
                    onError = { /* Manejo de errores si querés */ }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar cambios")
        }

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}
