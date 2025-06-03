package com.tramis.qpa.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {

    private val _salasAccedidas = MutableStateFlow<List<Pair<String, Map<String, Any>>>>(emptyList())
    val salasAccedidas: StateFlow<List<Pair<String, Map<String, Any>>>> = _salasAccedidas

    fun agregarSala(id: String, data: Map<String, Any>) {
        val actual = _salasAccedidas.value.toMutableList()
        if (actual.none { it.first == id }) {
            actual.add(id to data)
            _salasAccedidas.value = actual
        }
    }

    fun actualizarSalasActivas(onComplete: (Set<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("rooms").get().addOnSuccessListener { snapshot ->
            val activas = snapshot.documents.mapNotNull { it.id }.toSet()
            onComplete(activas)
        }
    }
}
