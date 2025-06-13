package com.tramis.qpa.screens

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No has iniciado sesión")
        }
        return
    }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(user.uid) {
        val snap = db.collection("users").document(user.uid).get().await()
        fullName = snap.getString("fullName") ?: ""
        nickname = snap.getString("nickname") ?: ""
        phone = snap.getString("phone") ?: ""
        photoUrl = snap.getString("photoUrl")
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedUri = it
            bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
        }
    }

    fun saveProfile() {
        scope.launch {
            val data = mapOf(
                "fullName" to fullName,
                "nickname" to nickname,
                "phone" to phone
            )
            db.collection("users").document(user.uid).set(data, SetOptions.merge()).await()

            selectedUri?.let { uri ->
                bitmap?.let { bmp ->
                    val baos = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, baos)
                    val bytes = baos.toByteArray()
                    val ref = storage.reference.child("profile_pictures/${user.uid}.jpg")
                    ref.putBytes(bytes).await()
                    val url = ref.downloadUrl.await().toString()
                    db.collection("users").document(user.uid)
                        .set(mapOf("photoUrl" to url), SetOptions.merge()).await()
                    photoUrl = url
                }
            }

            snackbarHostState.showSnackbar("Perfil actualizado")
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    bitmap != null -> Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    photoUrl != null -> Image(
                        painter = rememberAsyncImagePainter(photoUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    else -> Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Apodo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { saveProfile() }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar cambios")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
