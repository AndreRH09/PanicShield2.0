package com.example.panicshield.ui.screen.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Data class para información de ubicación
data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val isLocationActive: Boolean
)

// Data class para el estado principal de la UI
data class HomeUiState(
    val isPanicActivated: Boolean = false,
    val connectionStatus: String = "Conectado",
    val isLoading: Boolean = false
)

class HomeViewModel : ViewModel() {

    // StateFlow para el estado principal de la UI
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // StateFlow para la información de ubicación
    private val _locationInfo = MutableStateFlow<LocationInfo?>(null)
    val locationInfo: StateFlow<LocationInfo?> = _locationInfo.asStateFlow()

    init {
        loadInitialData()
    }

    //DEberia de ser automatico si cambia la variable  , si la funcion registra
    //
    /**
     * Carga datos iniciales de prueba
     */
    private fun loadInitialData() {
        // Datos de prueba para la ubicación (Arequipa, Perú)
        val mockLocation = LocationInfo(
            latitude = -16.4090,
            longitude = -71.5375,
            address = "Arequipa, Perú",
            isLocationActive = true
        )

        _locationInfo.value = mockLocation
    }

    /**
     * Alterna el estado del botón de pánico
     */
    fun togglePanicButton() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            isPanicActivated = !currentState.isPanicActivated
        )
    }

    /**
     * Obtiene el estado actual de la ubicación
     * @return String con el estado de la ubicación
     */
    fun getLocationStatus(): String {
        return if (_locationInfo.value?.isLocationActive == true) {
            "Activa"
        } else {
            "Inactiva"
        }
    }

    /**
     * Obtiene el estado actual del sistema
     * @return String con el estado del sistema
     */
    fun getCurrentStatus(): String {
        return if (_uiState.value.isPanicActivated) {
            "En Emergencia"
        } else {
            "Seguro"
        }
    }

    /**
     * Actualiza el estado de conexión
     * @param status Nuevo estado de conexión
     */
    fun updateConnectionStatus(status: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(connectionStatus = status)
    }

    /**
     * Actualiza la información de ubicación
     * @param newLocation Nueva información de ubicación
     */
    fun updateLocation(newLocation: LocationInfo) {
        _locationInfo.value = newLocation
    }

    override fun onCleared() {
        super.onCleared()
        // Limpiar recursos si es necesario
    }
}