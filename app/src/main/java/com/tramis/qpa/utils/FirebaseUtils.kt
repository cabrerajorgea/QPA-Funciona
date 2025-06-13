
package com.tramis.qpa.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await

data class CameraMoveRequest(val location: LatLng, val triggerKey: Int)

@SuppressLint("MissingPermission")
@Composable
fun getCurrentLocation(): LatLng? {
    val context = LocalContext.current
    var location by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val loc = fusedLocationClient
            .getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                null
            )
            .await()
        location = loc?.let { LatLng(it.latitude, it.longitude) }
    }

    return location
}

@SuppressLint("MissingPermission")
suspend fun fetchCurrentLocation(context: Context): LatLng? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val loc = fusedLocationClient
        .getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
        .await()
    return loc?.let { LatLng(it.latitude, it.longitude) }
}

@Composable
fun useSalasListener(): List<Pair<String, Map<String, Any>>> {
    val salas = remember { mutableStateListOf<Pair<String, Map<String, Any>>>() }
    val db = FirebaseFirestore.getInstance()
    DisposableEffect(Unit) {
        val listener = db.collection("rooms")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    salas.clear()
                    snapshot.documents.forEach { doc ->
                        doc.data?.let { salas.add(doc.id to it) }
                    }
                }
            }
        onDispose { listener.remove() }
    }
    return salas
}

@Composable
fun useSalaInfo(salaId: String): Map<String, Any>? {
    var sala by remember { mutableStateOf<Map<String, Any>?>(null) }
    val db = FirebaseFirestore.getInstance()
    LaunchedEffect(salaId) {
        val snap = db.collection("rooms").document(salaId).get().await()
        sala = snap.data
    }
    return sala
}

@Composable
fun useMensajesListener(roomId: String): List<Map<String, Any>> {
    val mensajes = remember { mutableStateListOf<Map<String, Any>>() }
    val db = FirebaseFirestore.getInstance()
    DisposableEffect(roomId) {
        val listener = db.collection("rooms").document(roomId).collection("messages")
            .orderBy("sentAt")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    mensajes.clear()
                    mensajes.addAll(snapshot.documents.mapNotNull { it.data })
                }
            }
        onDispose { listener.remove() }
    }
    return mensajes
}

@Composable
fun useUsuariosEnSalaListener(salaId: String): List<Map<String, Any>> {
    val usuarios = remember { mutableStateListOf<Map<String, Any>>() }
    val db = FirebaseFirestore.getInstance()
    DisposableEffect(salaId) {
        val listener = db.collection("rooms").document(salaId)
            .collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    usuarios.clear()
                    usuarios.addAll(snapshot.documents.mapNotNull { it.data })
                }
            }
        onDispose { listener.remove() }
    }
    return usuarios
}

fun enviarMensaje(
    roomId: String,
    senderId: String,
    senderName: String,
    texto: String,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val data = mapOf(
        "senderId" to senderId,
        "senderName" to senderName,
        "text" to texto,
        "sentAt" to Timestamp.now()
    )
    db.collection("rooms").document(roomId).collection("messages")
        .add(data)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onError(it) }
}

fun crearSala(
    nombre: String,
    lat: Double,
    lon: Double,
    creatorId: String,
    soloLocales: Boolean,
    soloVisibles: Boolean,
    totem: String,
    onSuccess: (String, Map<String, Any>) -> Unit,
    onError: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val data = hashMapOf(
        "name" to nombre,
        "creatorId" to creatorId,
        "location" to GeoPoint(lat, lon),
        "soloLocales" to soloLocales,
        "soloVisibles" to soloVisibles,
        "totem" to totem,
        "createdAt" to Timestamp.now()
    )
    db.collection("rooms")
        .add(data)
        .addOnSuccessListener { ref -> onSuccess(ref.id, data) }
        .addOnFailureListener { e -> onError(e) }
}
fun editarSala(
    salaId: String,
    nombre: String,
    soloVisibles: Boolean,
    totem: String,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val data = mapOf(
        "name" to nombre,
        "soloVisibles" to soloVisibles,
        "totem" to totem
    )
    db.collection("rooms").document(salaId)
        .update(data)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onError(it) }
}
