
package com.tramis.qpa.utils

import android.content.Context
import android.graphics.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.compose.*
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.MapProperties
import com.google.android.gms.maps.model.MapStyleOptions
import androidx.lifecycle.LifecycleOwner


import com.tramis.qpa.utils.CameraMoveRequest

fun createEmojiBitmap(context: Context, emoji: String, size: Float = 80f): BitmapDescriptor {
    val paint = Paint().apply {
        textSize = size
        isAntiAlias = true
        color = android.graphics.Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    val bounds = Rect()
    paint.getTextBounds(emoji, 0, emoji.length, bounds)

    val bmpWidth = bounds.width() + 40  // más padding
    val bmpHeight = bounds.height() + 60  // más alto para evitar corte de descendentes

    val bmp = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)

    val x = bmpWidth / 2f
    val y = bmpHeight / 2f - (paint.descent() + paint.ascent()) / 2f

    canvas.drawText(emoji, x, y, paint)

    return BitmapDescriptorFactory.fromBitmap(bmp)
}

@Composable
fun rememberDayNightMapProperties(): MapProperties {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var properties by remember { mutableStateOf(MapProperties()) }


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _: LifecycleOwner, event ->
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_CREATE) {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                val styleRes = if (hour in 6..18) {
                    com.tramis.qpa.R.raw.map_style_day
                } else {
                    com.tramis.qpa.R.raw.map_style_night
                }
                val style = MapStyleOptions.loadRawResourceStyle(context, styleRes)
                properties = MapProperties(mapStyleOptions = style)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return properties
}


@Composable
fun SimpleMapWithSalas(
    salas: List<Pair<String, Map<String, Any>>>,
    cameraMoveRequest: CameraMoveRequest?,
    onMarkerClick: (String, Map<String, Any>) -> Unit,
    onGrupoClick: (LatLng, List<Pair<String, Map<String, Any>>>) -> Unit,
    userLocation: LatLng? = null
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(cameraMoveRequest?.triggerKey) {
        cameraMoveRequest?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it.location, 16f)
            )
        }
    }

    val agrupadas = salas.groupBy {
        val geo = it.second["location"] as? GeoPoint
        val lat = geo?.latitude?.let { d -> String.format("%.4f", d) } ?: "0"
        val lon = geo?.longitude?.let { d -> String.format("%.4f", d) } ?: "0"
        "$lat,$lon"
    }

    val mapProperties = rememberDayNightMapProperties()

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties
    ) {
        userLocation?.let {
            val icon = remember { createEmojiBitmap(context, "🦉") }
            Marker(state = MarkerState(position = it), icon = icon)
        }
        agrupadas.forEach { (_, grupo) ->
            val primero = grupo.firstOrNull()?.second ?: return@forEach
            val geo = primero["location"] as? GeoPoint ?: return@forEach
            val baseLat = geo.latitude
            val baseLng = geo.longitude
            val latLng = LatLng(baseLat, baseLng)

            if (grupo.size > 5) {
                val emoji = "🔥 x${grupo.size}"
                val icon = remember(emoji) { createEmojiBitmap(context, emoji, size = 110f) }

                Circle(
                    center = latLng,
                    radius = 100.0,
                    strokeColor = Color.Red,
                    fillColor = Color.Red.copy(alpha = 0.2f),
                    strokeWidth = 2f
                )

                Marker(
                    state = MarkerState(position = latLng),
                    icon = icon,
                    onClick = {
                        onGrupoClick(latLng, grupo)
                        true
                    }
                )
            } else {
                grupo.forEachIndexed { index, (id, data) ->
                    val geoSala = data["location"] as? GeoPoint ?: return@forEachIndexed
                    val actividad = (data["usuarios"] as? List<*>)?.size ?: 0

                    val offset = 0.0001 * (index % 3)
                    val latDesplazado = geoSala.latitude + offset
                    val lngDesplazado = geoSala.longitude + offset
                    val latLngSala = LatLng(latDesplazado, lngDesplazado)

                    val color = when {
                        actividad >= 8 -> Color(0xFFD50000)
                        actividad >= 4 -> Color(0xFFFFEB3B)
                        else -> Color(0xFF00C853)
                    }

                    Circle(
                        center = latLngSala,
                        radius = 100.0,
                        strokeColor = color,
                        fillColor = color.copy(alpha = 0.25f),
                        strokeWidth = 4f
                    )

                    val emoji = data["totem"] as? String ?: "📍"
                    val icon = remember("$emoji-$index") { createEmojiBitmap(context, emoji) }

                    Marker(
                        state = MarkerState(position = latLngSala),
                        icon = icon,
                        onClick = {
                            onMarkerClick(id, data)
                            true
                        }
                    )
                }
            }
        }
    }
}
