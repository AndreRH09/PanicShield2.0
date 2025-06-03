package com.example.panicshield.ui.screen.history

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HistoryEntry(
    val id: String,
    val date: String,
    val time: String,
    val type: String,
    val status: String,
    val location: String
)


data class HistoryUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class HistoryViewModel : ViewModel() {


    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()


    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadHistoryData()
    }

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


    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }


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

    fun getGroupedEntries(): Map<String, List<HistoryEntry>> {
        val filteredEntries = getFilteredEntries()
        return mapOf(
            "Esta Semana" to filteredEntries.filter {
                it.date.contains("11/05/2025") || it.date.contains("10/05/2025")
            },
            "Este Mes" to filteredEntries.filter {
                it.date.contains("02/05/2025") && !it.date.contains("11/05/2025") && !it.date.contains("10/05/2025")
            },
        ).filter { it.value.isNotEmpty() }
    }
}