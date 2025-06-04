package com.tramis.qpa.utils

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

@Composable
fun SolicitarPermisoUbicacion(onPermisoConcedido: () -> Unit) {
    val context = LocalContext.current
    val permisoLocation = Manifest.permission.ACCESS_FINE_LOCATION
    val permisoConcedido = ContextCompat.checkSelfPermission(
        context,
        permisoLocation
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermisoConcedido()
        } else {
            Toast.makeText(
                context,
                "Se necesita permiso de ubicación para usar esta función.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!permisoConcedido) {
            launcher.launch(permisoLocation)
        } else {
            onPermisoConcedido()
        }
    }
}

