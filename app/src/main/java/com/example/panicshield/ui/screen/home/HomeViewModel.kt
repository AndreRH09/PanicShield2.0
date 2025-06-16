package com.example.panicshield.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.repository.EmergencyResult
import com.example.panicshield.domain.usecase.EmergencyUseCase
import com.example.panicshield.domain.usecase.LocationUseCase
import com.example.panicshield.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val emergencyUseCase: EmergencyUseCase,
    private val locationUseCase: LocationUseCase,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUIState())
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    private val _locationInfo = MutableStateFlow<LocationInfo?>(null)
    val locationInfo: StateFlow<LocationInfo?> = _locationInfo.asStateFlow()

    // Estados de autenticación
    private val _currentUserId = MutableStateFlow<String?>(null)
    private val _currentAccessToken = MutableStateFlow<String?>(null)
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

        viewModelScope.launch {
            tokenManager.getUserId().collect { userId ->
                _currentUserId.value = userId
            }
        }

        viewModelScope.launch {
            tokenManager.getAccessToken().collect { token ->
                _currentAccessToken.value = token

                if (token != null && _currentUserId.value != null) {
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
                    emergencyStatus = emergency?.status ?: EmergencyStatus.INACTIVE,
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
            val token = _currentAccessToken.value
            val userId = _currentUserId.value

            if (token == null || userId == null) {
                return@launch
            }

            _uiState.value = _uiState.value.copy(connectionStatus = ConnectionStatus.CONNECTING)

            when (val result = emergencyUseCase.getCurrentActiveEmergency(token, userId)) {
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
                    // Ya manejado arriba
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
            val token = _currentAccessToken.value
            val userId = _currentUserId.value

            if (token == null || userId == null) {
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

            when (val result = emergencyUseCase.createPanicAlert(
                authToken = token,
                userId = userId,
                location = currentLocation,
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
                    // Ya manejado arriba
                }
            }
        }
    }

    private fun deactivatePanic() {
        viewModelScope.launch {
            val token = _currentAccessToken.value
            val emergencyIdString = _uiState.value.emergencyId

            if (token == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error de autenticación"
                )
                return@launch
            }

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

            when (val result = emergencyUseCase.cancelEmergency(token, emergencyId)) {
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
                    // Ya manejado arriba
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

    fun getCurrentUserId(): String? {
        return _currentUserId.value
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

    fun retryConnection() {
        viewModelScope.launch {
            checkForActiveEmergency()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup si es necesario
    }
}