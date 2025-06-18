package com.example.panicshield.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.domain.usecase.EmergencyResult
import com.example.panicshield.domain.usecase.HistoryUseCase
import com.example.panicshield.domain.usecase.EmergencyHistory
import com.example.panicshield.domain.usecase.EmergencyStatistics
import com.example.panicshield.domain.model.EmergencyStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

// Enum de filtros de tiempo (si no existe ya en otro lugar)
enum class HistoryTimeFilter {
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    ALL
}

data class HistoryUIState(
    val emergencyHistory: List<EmergencyHistory> = emptyList(),
    val filteredEmergencies: List<EmergencyHistory> = emptyList(),
    val selectedEmergency: EmergencyHistory? = null,
    val statistics: EmergencyStatistics? = null,
    val currentFilter: HistoryTimeFilter = HistoryTimeFilter.THIS_WEEK,
    val statusFilter: EmergencyStatus? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyUseCase: HistoryUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUIState())
    val uiState: StateFlow<HistoryUIState> = _uiState.asStateFlow()

    init {
        observeAuthenticationState()
        observeFiltersAndSearch()
    }

    private fun observeAuthenticationState() {
        viewModelScope.launch {
            tokenManager.isLoggedIn().collect { isLoggedIn ->
                _uiState.value = _uiState.value.copy(isAuthenticated = isLoggedIn)

                if (isLoggedIn) {
                    // Auto-cargar datos cuando el usuario esté autenticado
                    loadEmergencyHistory()
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Debes iniciar sesión para ver el historial",
                        emergencyHistory = emptyList(),
                        filteredEmergencies = emptyList()
                    )
                }
            }
        }
    }

    private fun observeFiltersAndSearch() {
        viewModelScope.launch {
            // Combinar cambios de filtros y búsqueda
            combine(
                _uiState,
                _uiState
            ) { state1, state2 ->
                state1 to state2
            }.collect { (state, _) ->
                val filtered = applyFiltersAndSearch(
                    emergencies = state.emergencyHistory,
                    timeFilter = state.currentFilter,
                    statusFilter = state.statusFilter,
                    searchQuery = state.searchQuery
                )

                if (filtered != state.filteredEmergencies) {
                    _uiState.value = state.copy(filteredEmergencies = filtered)
                }
            }
        }
    }

    // ✅ FUNCIÓN: Cargar historial de emergencias (sin parámetros auth)
    fun loadEmergencyHistory() {
        viewModelScope.launch {
            if (!_uiState.value.isAuthenticated) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Usuario no autenticado"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = historyUseCase.getEmergencyHistory()) {
                is EmergencyResult.Success -> {
                    val emergencies = result.data
                    val filtered = applyFiltersAndSearch(
                        emergencies = emergencies,
                        timeFilter = _uiState.value.currentFilter,
                        statusFilter = _uiState.value.statusFilter,
                        searchQuery = _uiState.value.searchQuery
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        emergencyHistory = emergencies,
                        filteredEmergencies = filtered,
                        errorMessage = null
                    )

                    // Cargar estadísticas
                    loadStatistics()
                }
                is EmergencyResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar historial: ${result.exception.message}"
                    )
                }
                is EmergencyResult.Loading -> {
                    // Ya manejado arriba
                }
            }
        }
    }

    // ✅ FUNCIÓN: Cargar estadísticas
    private fun loadStatistics() {
        viewModelScope.launch {
            when (val result = historyUseCase.getEmergencyStatistics()) {
                is EmergencyResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        statistics = result.data
                    )
                }
                is EmergencyResult.Error -> {
                    // No mostrar error para estadísticas, es opcional
                    println("Error loading statistics: ${result.exception.message}")
                }
                is EmergencyResult.Loading -> {
                    // No necesario
                }
            }
        }
    }

    // ✅ FUNCIÓN: Seleccionar emergencia para ver detalles
    fun selectEmergency(emergency: EmergencyHistory) {
        _uiState.value = _uiState.value.copy(
            selectedEmergency = emergency
        )
    }

    // ✅ FUNCIÓN: Limpiar emergencia seleccionada
    fun clearSelectedEmergency() {
        _uiState.value = _uiState.value.copy(
            selectedEmergency = null
        )
    }

    // ✅ FUNCIÓN: Cambiar filtro de tiempo
    fun setTimeFilter(filter: HistoryTimeFilter) {
        val currentState = _uiState.value
        val filtered = applyFiltersAndSearch(
            emergencies = currentState.emergencyHistory,
            timeFilter = filter,
            statusFilter = currentState.statusFilter,
            searchQuery = currentState.searchQuery
        )

        _uiState.value = currentState.copy(
            currentFilter = filter,
            filteredEmergencies = filtered
        )
    }

    // ✅ FUNCIÓN: Cambiar filtro de estado
    fun setStatusFilter(status: EmergencyStatus?) {
        val currentState = _uiState.value
        val filtered = applyFiltersAndSearch(
            emergencies = currentState.emergencyHistory,
            timeFilter = currentState.currentFilter,
            statusFilter = status,
            searchQuery = currentState.searchQuery
        )

        _uiState.value = currentState.copy(
            statusFilter = status,
            filteredEmergencies = filtered
        )
    }

    // ✅ FUNCIÓN: Cambiar query de búsqueda
    fun setSearchQuery(query: String) {
        val currentState = _uiState.value
        val filtered = applyFiltersAndSearch(
            emergencies = currentState.emergencyHistory,
            timeFilter = currentState.currentFilter,
            statusFilter = currentState.statusFilter,
            searchQuery = query
        )

        _uiState.value = currentState.copy(
            searchQuery = query,
            filteredEmergencies = filtered
        )
    }

    // ✅ FUNCIÓN: Obtener detalles de emergencia específica
    fun getEmergencyDetails(emergencyId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = historyUseCase.getEmergencyById(emergencyId)) {
                is EmergencyResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedEmergency = result.data,
                        errorMessage = null
                    )
                }
                is EmergencyResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar detalles: ${result.exception.message}"
                    )
                }
                is EmergencyResult.Loading -> {
                    // Ya manejado arriba
                }
            }
        }
    }

    // ✅ FUNCIÓN: Buscar en historial
    fun searchEmergencies(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                setSearchQuery("")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = historyUseCase.searchEmergencyHistory(query)) {
                is EmergencyResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        filteredEmergencies = result.data,
                        searchQuery = query
                    )
                }
                is EmergencyResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error en búsqueda: ${result.exception.message}"
                    )
                }
                is EmergencyResult.Loading -> {
                    // Ya manejado arriba
                }
            }
        }
    }

    // ✅ FUNCIÓN: Obtener emergencias por rango de fechas
    fun getEmergenciesByDateRange(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = historyUseCase.getEmergencyHistoryByDateRange(startDate, endDate)) {
                is EmergencyResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        filteredEmergencies = result.data
                    )
                }
                is EmergencyResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al filtrar por fecha: ${result.exception.message}"
                    )
                }
                is EmergencyResult.Loading -> {
                    // Ya manejado arriba
                }
            }
        }
    }

    // ✅ FUNCIÓN: Obtener emergencias por estado
    fun getEmergenciesByStatus(status: EmergencyStatus) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            when (val result = historyUseCase.getEmergencyHistoryByStatus(status)) {
                is EmergencyResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        filteredEmergencies = result.data
                    )
                }
                is EmergencyResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al filtrar por estado: ${result.exception.message}"
                    )
                }
                is EmergencyResult.Loading -> {
                    // Ya manejado arriba
                }
            }
        }
    }

    // ✅ FUNCIÓN: Limpiar errores
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // ✅ FUNCIÓN: Refrescar datos
    fun refreshData() {
        loadEmergencyHistory()
    }

    // ✅ FUNCIÓN: Limpiar filtros
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            currentFilter = HistoryTimeFilter.ALL,
            statusFilter = null,
            searchQuery = "",
            filteredEmergencies = _uiState.value.emergencyHistory
        )
    }

    // ===== FUNCIONES PRIVADAS =====

    private fun applyFiltersAndSearch(
        emergencies: List<EmergencyHistory>,
        timeFilter: HistoryTimeFilter,
        statusFilter: EmergencyStatus?,
        searchQuery: String
    ): List<EmergencyHistory> {
        var filtered = emergencies

        // ✅ APLICAR FILTRO DE TIEMPO
        filtered = when (timeFilter) {
            HistoryTimeFilter.THIS_WEEK -> {
                val calendar = Calendar.getInstance()
                val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
                val currentYear = calendar.get(Calendar.YEAR)

                filtered.filter { emergency ->
                    calendar.timeInMillis = emergency.createdAt
                    calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek &&
                            calendar.get(Calendar.YEAR) == currentYear
                }
            }
            HistoryTimeFilter.THIS_MONTH -> {
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)

                filtered.filter { emergency ->
                    calendar.timeInMillis = emergency.createdAt
                    calendar.get(Calendar.MONTH) == currentMonth &&
                            calendar.get(Calendar.YEAR) == currentYear
                }
            }
            HistoryTimeFilter.LAST_MONTH -> {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MONTH, -1)
                val lastMonth = calendar.get(Calendar.MONTH)
                val lastMonthYear = calendar.get(Calendar.YEAR)

                filtered.filter { emergency ->
                    calendar.timeInMillis = emergency.createdAt
                    calendar.get(Calendar.MONTH) == lastMonth &&
                            calendar.get(Calendar.YEAR) == lastMonthYear
                }
            }
            HistoryTimeFilter.ALL -> filtered
        }

        // ✅ APLICAR FILTRO DE ESTADO
        statusFilter?.let { status ->
            filtered = filtered.filter { emergency ->
                emergency.status == status
            }
        }

        // ✅ APLICAR BÚSQUEDA
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase().trim()
            filtered = filtered.filter { emergency ->
                emergency.emergencyType.lowercase().contains(query) ||
                        emergency.message?.lowercase()?.contains(query) == true ||
                        emergency.address?.lowercase()?.contains(query) == true ||
                        emergency.status.name.lowercase().contains(query)
            }
        }

        // ✅ ORDENAR POR FECHA (MÁS RECIENTE PRIMERO)
        return filtered.sortedByDescending { it.createdAt }
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup si es necesario
    }
}