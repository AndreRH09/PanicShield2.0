package com.example.panicshield.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.remote.repository.EmergencyResult
import com.example.panicshield.domain.usecase.EmergencyUseCase
import com.example.panicshield.domain.usecase.LocationUseCase
import com.example.panicshield.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI State para Home
data class HomeUIState(
    val isPanicActivated: Boolean = false,
    val isLoading: Boolean = false,
    val emergencyId: String? = null,
    val emergencyStatus: EmergencyStatus = EmergencyStatus.INACTIVE,
    val lastPanicTime: Long? = null,
    val lastSyncTime: Long? = null,
    val errorMessage: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val networkAvailable: Boolean = false,
    val isLocationPermissionGranted: Boolean = false
)

// Estados de conexión
enum class ConnectionStatus {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    ERROR
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val emergencyUseCase: EmergencyUseCase,
    private val locationUseCase: LocationUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUIState())
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()
    private val _connectivityJob = MutableStateFlow<Job?>(null)

    private val _locationInfo = MutableStateFlow<LocationInfo?>(null)
    val locationInfo: StateFlow<LocationInfo?> = _locationInfo.asStateFlow()

    // Estados de autenticación
    private val _isAuthenticated = MutableStateFlow(false)

    init {
        observeAuthenticationState()
        observePanicState()
        observeLocationUpdates()
        initializeLocation()
    }

    private fun observeAuthenticationState() {
        viewModelScope.launch {
            tokenManager.isLoggedIn().collect { isLoggedIn ->
                _isAuthenticated.value = isLoggedIn

                if (!isLoggedIn) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Debes iniciar sesión para usar la app",
                        connectionStatus = ConnectionStatus.ERROR
                    )
                } else {
                    checkForActiveEmergency()
                }
            }
        }
    }

    private fun observePanicState() {
        viewModelScope.launch {
            emergencyUseCase.isPanicActive().collect { isActive ->
                _uiState.value = _uiState.value.copy(
                    isPanicActivated = isActive,
                    emergencyStatus = if (isActive) EmergencyStatus.ACTIVE else EmergencyStatus.INACTIVE
                )
            }
        }

        viewModelScope.launch {
            emergencyUseCase.getCurrentEmergency().collect { emergency ->
                _uiState.value = _uiState.value.copy(
                    emergencyId = emergency?.id?.toString(),
                    emergencyStatus = emergency?.statusEnum ?: EmergencyStatus.INACTIVE,
                    lastSyncTime = System.currentTimeMillis()
                )
            }
        }
    }

    private fun observeLocationUpdates() {
        viewModelScope.launch {
            locationUseCase.getCurrentLocation().collect { location ->
                _locationInfo.value = location
                _uiState.value = _uiState.value.copy(
                    isLocationPermissionGranted = location?.isLocationActive ?: false
                )
            }
        }
    }

    private fun initializeLocation() {
        viewModelScope.launch {
            locationUseCase.updateLocation()
        }
    }

    private fun checkForActiveEmergency() {
        viewModelScope.launch {
            if (!_isAuthenticated.value) {
                return@launch
            }

            _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.CONNECTING)

            when (val result = emergencyUseCase.getCurrentActiveEmergency()) {
                is EmergencyResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        connectionStatus = ConnectionStatus.CONNECTED,
                        networkAvailable = true,
                        errorMessage = null
                    )
                }
                is EmergencyResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        connectionStatus = ConnectionStatus.ERROR,
                        networkAvailable = false,
                        errorMessage = "Error de conexión: ${result.exception.message}"
                    )
                }
                is EmergencyResult.Loading -> {
                }
            }
        }
    }

    // ==================== FUNCIONES PÚBLICAS ====================

    fun togglePanicButton() {
        if (_uiState.value.isPanicActivated) {
            deactivatePanic()
        } else {
            activatePanic()
        }
    }

    fun activatePanicWithLongPress() {
        activatePanic()
    }

    private fun activatePanic() {
        viewModelScope.launch {
            if (!_isAuthenticated.value) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Debes iniciar sesión para activar emergencias"
                )
                return@launch
            }

            val currentLocation = _locationInfo.value
            if (currentLocation == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No se pudo obtener la ubicación. Intenta nuevamente."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                emergencyStatus = EmergencyStatus.PENDING,
                errorMessage = null
            )

            // Convertir LocationInfo a LocationInfo de EmergencyUseCase si es necesario
            val locationForEmergency = com.example.panicshield.domain.usecase.LocationInfo(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                address = currentLocation.address,
                accuracy = currentLocation.accuracy
            )

            when (val result = emergencyUseCase.createPanicAlert(
                location = locationForEmergency,
                message = "Emergencia activada desde botón de pánico"
            )) {
                is EmergencyResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPanicActivated = true,
                        emergencyId = result.data.id?.toString(),
                        emergencyStatus = EmergencyStatus.ACTIVE,
                        lastPanicTime = System.currentTimeMillis(),
                        connectionStatus = ConnectionStatus.CONNECTED
                    )
                }
                is EmergencyResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        emergencyStatus = EmergencyStatus.INACTIVE,
                        errorMessage = "Error al crear emergencia: ${result.exception.message}",
                        connectionStatus = if (result.code != null) ConnectionStatus.ERROR else ConnectionStatus.DISCONNECTED
                    )
                }
                is EmergencyResult.Loading -> {
                }
            }
        }
    }

    private fun deactivatePanic() {
        viewModelScope.launch {
            val emergencyIdString = _uiState.value.emergencyId

            if (emergencyIdString == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No hay emergencia activa para cancelar"
                )
                return@launch
            }

            val emergencyId = emergencyIdString.toLongOrNull()
            if (emergencyId == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "ID de emergencia inválido"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isLoading = true,
                emergencyStatus = EmergencyStatus.CANCELLING
            )

            when (val result = emergencyUseCase.cancelEmergency(emergencyId)) {
                is EmergencyResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isPanicActivated = false,
                        emergencyStatus = EmergencyStatus.CANCELLED,
                        emergencyId = null
                    )
                }
                is EmergencyResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cancelar emergencia: ${result.exception.message}"
                    )
                }
                is EmergencyResult.Loading -> {

                }
            }
        }
    }

    // ==================== FUNCIONES DE UI ====================

    fun getLocationStatus(): String {
        return if (_locationInfo.value?.isLocationActive == true) {
            "Activa"
        } else {
            "Inactiva"
        }
    }

    fun getCurrentStatus(): String {
        return when (_uiState.value.emergencyStatus) {
            EmergencyStatus.ACTIVE -> "Alerta Activa"
            EmergencyStatus.PENDING -> "Enviando..."
            EmergencyStatus.CANCELLING -> "Cancelando..."
            EmergencyStatus.CANCELLED -> "Cancelada"
            EmergencyStatus.COMPLETED -> "Completada"
            EmergencyStatus.INACTIVE -> "Seguro"
        }
    }

    fun getAuthStatus(): String {
        return if (_isAuthenticated.value) {
            "Autenticado"
        } else {
            "No autenticado"
        }
    }

    fun getConnectionStatusText(): String {
        return when (_uiState.value.connectionStatus) {
            ConnectionStatus.CONNECTED -> "Conectado"
            ConnectionStatus.CONNECTING -> "Conectando..."
            ConnectionStatus.DISCONNECTED -> "Desconectado"
            ConnectionStatus.ERROR -> "Error de conexión"
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun refreshLocation() {
        viewModelScope.launch {
            locationUseCase.updateLocation()
        }
    }

    // ✅ NUEVA FUNCIÓN: Monitoreo de conectividad con suspend
    fun startConnectivityMonitoring() {
        // Cancelar job anterior si existe
        _connectivityJob.value?.cancel()

        val job = viewModelScope.launch {
            while (true) {
                checkNetworkConnectivity()
                delay(5000) // Verificar cada 5 segundos
            }
        }
        _connectivityJob.value = job
    }

    // ✅ NUEVA FUNCIÓN: Verificar conectividad de red
    private suspend fun checkNetworkConnectivity() {
        _uiState.value = _uiState.value.copy(
            connectionStatus = ConnectionStatus.CONNECTING
        )

        try {
            if (!_isAuthenticated.value) {
                _uiState.value = _uiState.value.copy(
                    connectionStatus = ConnectionStatus.ERROR,
                    errorMessage = "No hay credenciales de autenticación"
                )
                return
            }

            // ✅ USAR SUSPEND para verificar conectividad
            when (val result = emergencyUseCase.testConnection()) {
                is EmergencyResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        connectionStatus = ConnectionStatus.CONNECTED,
                        networkAvailable = true,
                        errorMessage = null
                    )
                }
                is EmergencyResult.Error -> {
                    val connectionStatus = when {
                        result.exception.message?.contains("UnknownHostException") == true ||
                                result.exception.message?.contains("ConnectException") == true ->
                            ConnectionStatus.DISCONNECTED

                        result.exception.message?.contains("SocketTimeoutException") == true ||
                                result.exception.message?.contains("TimeoutException") == true ->
                            ConnectionStatus.ERROR

                        result.code == 401 || result.code == 403 ->
                            ConnectionStatus.ERROR

                        else -> ConnectionStatus.ERROR
                    }

                    _uiState.value = _uiState.value.copy(
                        connectionStatus = connectionStatus,
                        networkAvailable = false,
                        errorMessage = when (connectionStatus) {
                            ConnectionStatus.DISCONNECTED -> "Sin conexión a internet"
                            ConnectionStatus.ERROR -> "Error de servidor: ${result.exception.message}"
                            else -> "Error desconocido"
                        }
                    )
                }
                is EmergencyResult.Loading -> {
                }
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                connectionStatus = ConnectionStatus.ERROR,
                networkAvailable = false,
                errorMessage = "Error de conectividad: ${e.message}"
            )
        }
    }

    // ✅ FUNCIÓN EXISTENTE MEJORADA: retryConnection
    fun retryConnection() {
        viewModelScope.launch {
            checkNetworkConnectivity()
        }
    }

    // ✅ NUEVA FUNCIÓN: Detener monitoreo
    fun stopConnectivityMonitoring() {
        _connectivityJob.value?.cancel()
        _connectivityJob.value = null
    }

    // ✅ ACTUALIZAR onCleared
    override fun onCleared() {
        super.onCleared()
        stopConnectivityMonitoring()
    }
}