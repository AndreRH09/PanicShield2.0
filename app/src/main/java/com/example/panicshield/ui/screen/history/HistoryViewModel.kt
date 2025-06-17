package com.example.panicshield.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.repository.EmergencyResult
import com.example.panicshield.domain.model.EmergencyHistory
import com.example.panicshield.domain.usecase.HistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class HistoryUIState(
    val emergencyHistory: List<EmergencyHistory> = emptyList(),
    val filteredEmergencies: List<EmergencyHistory> = emptyList(),
    val selectedEmergency: EmergencyHistory? = null,
    val currentFilter: TimeFilter = TimeFilter.THIS_WEEK,
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

    private val _currentUserId = MutableStateFlow<String?>(null)
    private val _currentAccessToken = MutableStateFlow<String?>(null)

    init {
        observeAuthenticationState()
        observeFiltersAndSearch()
    }

    private fun observeAuthenticationState() {
        viewModelScope.launch {
            tokenManager.isLoggedIn().collect { isLoggedIn ->
                _uiState.value = _uiState.value.copy(isAuthenticated = isLoggedIn)

                if (!isLoggedIn) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Debes iniciar sesión para ver el historial"
                    )
                }
            }
        }

        viewModelScope.launch {
            tokenManager.getUserId().collect { userId ->
                _currentUserId.value = userId
            }
        }

        viewModelScope.launch {
            tokenManager.getAccessToken().collect { token ->
                _currentAccessToken.value = token
            }
        }
    }

    private fun observeFiltersAndSearch() {
        viewModelScope.launch {
            // ✅ Combinar filtros y búsqueda para actualizar lista filtrada
            combine(
                _uiState,
                _uiState
            ) { state1, state2 ->
                state1 to state2
            }.collect { (state, _) ->
                val filtered = applyFiltersAndSearch(
                    emergencies = state.emergencyHistory,
                    filter = state.currentFilter,
                    searchQuery = state.searchQuery
                )

                if (filtered != state.filteredEmergencies) {
                    _uiState.value = state.copy(filteredEmergencies = filtered)
                }
            }
        }
    }

    // ✅ FUNCIÓN: Cargar historial de emergencias
    fun loadEmergencyHistory() {
        viewModelScope.launch {
            val token = _currentAccessToken.value
            val userId = _currentUserId.value

            if (token == null || userId == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No hay credenciales de autenticación"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = historyUseCase.getEmergencyHistory(token, userId)) {
                is EmergencyResult.Success -> {
                    val emergencies = result.data
                    val filtered = applyFiltersAndSearch(
                        emergencies = emergencies,
                        filter = _uiState.value.currentFilter,
                        searchQuery = _uiState.value.searchQuery
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        emergencyHistory = emergencies,
                        filteredEmergencies = filtered,
                        errorMessage = null
                    )
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

    // ✅ FUNCIÓN: Seleccionar emergencia para ver detalles
    fun selectEmergency(emergency: EmergencyHistory) {
        _uiState.value = _uiState.value.copy(
            selectedEmergency = emergency
        )
    }

    // ✅ FUNCIÓN: Limpiar emergencia seleccionada (volver a lista)
    fun clearSelectedEmergency() {
        _uiState.value = _uiState.value.copy(
            selectedEmergency = null
        )
    }

    // ✅ FUNCIÓN: Cambiar filtro de tiempo
    fun setTimeFilter(filter: TimeFilter) {
        val currentState = _uiState.value
        val filtered = applyFiltersAndSearch(
            emergencies = currentState.emergencyHistory,
            filter = filter,
            searchQuery = currentState.searchQuery
        )

        _uiState.value = currentState.copy(
            currentFilter = filter,
            filteredEmergencies = filtered
        )
    }

    // ✅ FUNCIÓN: Cambiar query de búsqueda
    fun setSearchQuery(query: String) {
        val currentState = _uiState.value
        val filtered = applyFiltersAndSearch(
            emergencies = currentState.emergencyHistory,
            filter = currentState.currentFilter,
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
            val token = _currentAccessToken.value

            if (token == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No hay credenciales de autenticación"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = historyUseCase.getEmergencyById(token, emergencyId)) {
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

    // ✅ FUNCIÓN: Limpiar errores
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // ✅ FUNCIÓN: Refrescar datos
    fun refreshData() {
        loadEmergencyHistory()
    }

    // ===== FUNCIONES PRIVADAS =====

    private fun applyFiltersAndSearch(
        emergencies: List<EmergencyHistory>,
        filter: TimeFilter,
        searchQuery: String
    ): List<EmergencyHistory> {
        var filtered = emergencies

        // ✅ APLICAR FILTRO DE TIEMPO
        filtered = when (filter) {
            TimeFilter.THIS_WEEK -> {
                val calendar = Calendar.getInstance()
                val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
                val currentYear = calendar.get(Calendar.YEAR)

                filtered.filter { emergency ->
                    calendar.timeInMillis = emergency.createdAt
                    calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek &&
                            calendar.get(Calendar.YEAR) == currentYear
                }
            }
            TimeFilter.THIS_MONTH -> {
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)

                filtered.filter { emergency ->
                    calendar.timeInMillis = emergency.createdAt
                    calendar.get(Calendar.MONTH) == currentMonth &&
                            calendar.get(Calendar.YEAR) == currentYear
                }
            }
            TimeFilter.LAST_MONTH -> {
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
            TimeFilter.ALL -> filtered
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