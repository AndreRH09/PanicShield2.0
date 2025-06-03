package com.example.panicshield.ui.screen.history

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Data class para cada entrada del historial
data class HistoryEntry(
    val id: String,
    val date: String,
    val time: String,
    val type: String,
    val status: String,
    val location: String
)

// Estado de la UI del historial
data class HistoryUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class HistoryViewModel : ViewModel() {

    // StateFlow para el estado principal del historial
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    // StateFlow para el término de búsqueda
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadHistoryData()
    }

    /**
     * Carga datos de historial de prueba
     */
    private fun loadHistoryData() {
        val mockEntries = listOf(
            HistoryEntry(
                id = "1",
                date = "Domingo 11/05/2025",
                time = "12:00 am",
                type = "Alerta Ubicación",
                status = "Completada",
                location = "Arequipa, Perú"
            ),
            HistoryEntry(
                id = "2",
                date = "Sábado 10/05/2025",
                time = "12:00 am",
                type = "Alerta Ubicación",
                status = "Completada",
                location = "Arequipa, Perú"
            ),
            HistoryEntry(
                id = "3",
                date = "Viernes 02/05/2025",
                time = "12:00 am",
                type = "Alerta Pánico",
                status = "Resuelta",
                location = "Arequipa, Perú"
            ),
            HistoryEntry(
                id = "4",
                date = "Sábado 10/05/2025",
                time = "12:00 am",
                type = "Alerta Ubicación",
                status = "Completada",
                location = "Arequipa, Perú"
            )
        )

        _uiState.value = _uiState.value.copy(entries = mockEntries)
    }

    /**
     * Actualiza el término de búsqueda
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /**
     * Obtiene entradas filtradas por búsqueda
     */
    fun getFilteredEntries(): List<HistoryEntry> {
        val currentState = _uiState.value
        return if (currentState.searchQuery.isBlank()) {
            currentState.entries
        } else {
            currentState.entries.filter { entry ->
                entry.type.contains(currentState.searchQuery, ignoreCase = true) ||
                        entry.location.contains(currentState.searchQuery, ignoreCase = true) ||
                        entry.date.contains(currentState.searchQuery, ignoreCase = true)
            }
        }
    }

    /**
     * Agrupa entradas por período de tiempo
     */
    fun getGroupedEntries(): Map<String, List<HistoryEntry>> {
        val filteredEntries = getFilteredEntries()
        return mapOf(
            "Esta Semana" to filteredEntries.filter {
                it.date.contains("11/05/2025") || it.date.contains("10/05/2025")
            },
            "Este Mes" to filteredEntries.filter {
                it.date.contains("02/05/2025") && !it.date.contains("11/05/2025") && !it.date.contains("10/05/2025")
            }
        ).filter { it.value.isNotEmpty() }
    }
}