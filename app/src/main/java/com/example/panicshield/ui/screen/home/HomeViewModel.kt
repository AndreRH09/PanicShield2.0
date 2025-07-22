package com.example.panicshield.ui.screen.home

import com.example.panicshield.data.local.SettingsDataStore
import kotlinx.coroutines.flow.combine
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.sms.UserHelper
import com.example.panicshield.data.sms.SmsHelper
import com.example.panicshield.data.remote.repository.EmergencyResult
import com.example.panicshield.data.sms.PermissionManager
import com.example.panicshield.domain.usecase.EmergencyUseCase
import com.example.panicshield.domain.usecase.LocationUseCase
import com.example.panicshield.domain.usecase.ContactUseCase
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
    val isLocationPermissionGranted: Boolean = false,
    val smsStatus: String? = null,
    val hasSmsPermission: Boolean = false
)

data class TapState(
    val tapCount: Int = 0,
    val isInTapWindow: Boolean = false,
    val windowStartTime: Long = 0L
)

data class AlertSettings(
    val moderateAlertTaps: Int = 2,
    val severeAlertTaps: Int = 3,
    val tapWindowDuration: Long = 2000L // 2 segundos
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
    private val contactUseCase: ContactUseCase, // ✅ Cambio: usar ContactUseCase en lugar de ContactDao
    private val tokenManager: TokenManager,
    private val userHelper: UserHelper,
    private val smsHelper: SmsHelper,
    private val settingsDataStore: SettingsDataStore // ✅ AGREGAR ESTA LÍNEA

) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUIState())
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()
    private val _connectivityJob = MutableStateFlow<Job?>(null)

    private val _locationInfo = MutableStateFlow<LocationInfo?>(null)
    val locationInfo: StateFlow<LocationInfo?> = _locationInfo.asStateFlow()

    // Estados de autenticación
    private val _isAuthenticated = MutableStateFlow(false)
    // Estados para lógica de toques rápidos
    private val _tapState = MutableStateFlow(TapState())
    private val _alertSettings = MutableStateFlow(AlertSettings())


    //MQTT
    private val _panicState = MutableStateFlow<PanicState>(PanicState.Idle)
    val panicState: StateFlow<PanicState> = _panicState
    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted = _permissionsGranted.asStateFlow()

    fun updatePermissionStatus(context: Context) {
        val permissionManager = PermissionManager(context)
        _permissionsGranted.value = permissionManager.hasAllPermissions()
    }

    init {
        observeAuthenticationState()
        observePanicState()
        observeLocationUpdates()
        initializeLocation()
        initializeSms()

        // ✅ AGREGAR ESTA LÍNEA:
        startConnectivityMonitoring()
        observeUserSettings() // ✅ AGREGAR ESTA LÍNEA

    }

    private fun observeUserSettings() {
        viewModelScope.launch {
            combine(
                settingsDataStore.moderateAlertTaps,
                settingsDataStore.severeAlertTaps
            ) { moderate, severe ->
                AlertSettings(
                    moderateAlertTaps = moderate,
                    severeAlertTaps = severe,
                    tapWindowDuration = 2000L
                )
            }.collect { settings ->
                _alertSettings.value = settings
                Log.d("HomeViewModel", "⚙️ Settings actualizados: Moderado=${settings.moderateAlertTaps}, Severo=${settings.severeAlertTaps}")
            }
        }
    }


    // ✅ FUNCIÓN: Inicializar SMS Manager
    private fun initializeSms() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "🔄 Inicializando SMS Manager...")

                // Verificar permisos SMS
                val hasPermission = smsHelper.hasSmsPermission()
                Log.d("HomeViewModel", "📱 Permisos SMS: $hasPermission")

                _uiState.value = _uiState.value.copy(hasSmsPermission = hasPermission)

                if (hasPermission) {
                    val isConnected = smsHelper.connect()
                    Log.d("HomeViewModel", "✅ SMS Manager listo: $isConnected")
                } else {
                    Log.w("HomeViewModel", "⚠️ Sin permisos SMS")
                }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Error inicializando SMS Manager: ${e.message}", e)
            }
        }
    }

    // NUEVA FUNCIÓN: Manejar toques del botón de emergencia
    fun handleEmergencyButtonTap() {
        val currentTime = System.currentTimeMillis()
        val currentTapState = _tapState.value
        val settings = _alertSettings.value

        when {
            !currentTapState.isInTapWindow -> {
                // Primer toque - iniciar ventana de tiempo
                Log.d("HomeViewModel", "👆 Primer toque - iniciando ventana de ${settings.tapWindowDuration}ms")
                _tapState.value = TapState(
                    tapCount = 1,
                    isInTapWindow = true,
                    windowStartTime = currentTime
                )

                // Iniciar timer para evaluar al final de la ventana
                startTapWindowTimer(settings)
            }

            currentTapState.isInTapWindow -> {
                // Toque adicional dentro de la ventana
                val newTapCount = currentTapState.tapCount + 1
                Log.d("HomeViewModel", "👆 Toque #$newTapCount dentro de la ventana")

                _tapState.value = currentTapState.copy(tapCount = newTapCount)
            }
        }
    }

    private fun startTapWindowTimer(settings: AlertSettings) {
        viewModelScope.launch {
            delay(settings.tapWindowDuration)

            val finalTapState = _tapState.value
            if (finalTapState.isInTapWindow) {
                Log.d("HomeViewModel", "⏰ Ventana cerrada con ${finalTapState.tapCount} toques")
                evaluateTapsAndCreateEmergency(finalTapState.tapCount, settings)
                resetTapState()
            }
        }
    }

    private fun evaluateTapsAndCreateEmergency(tapCount: Int, settings: AlertSettings) {
        Log.d("HomeViewModel", "🎯 Evaluando $tapCount toques (Moderado: ${settings.moderateAlertTaps}, Severo: ${settings.severeAlertTaps})")

        when {
            tapCount >= settings.severeAlertTaps -> {
                Log.d("HomeViewModel", "🚨 ALERTA SEVERA activada con $tapCount toques")
                activateEmergencyWithPriority("CRITICAL")
            }
            tapCount >= settings.moderateAlertTaps -> {
                Log.d("HomeViewModel", "⚠️ ALERTA MODERADA activada con $tapCount toques")
                activateEmergencyWithPriority("HIGH")
            }
            else -> {
                Log.d("HomeViewModel", "❌ Insuficientes toques ($tapCount). Mínimo requerido: ${settings.moderateAlertTaps}. NO se enviará alarma.")
                // ✅ NO hacer nada - no se crea emergencia
                showInsufficientTapsMessage(tapCount, settings.moderateAlertTaps)
            }
        }
    }

    private fun showInsufficientTapsMessage(actualTaps: Int, requiredTaps: Int) {
        val message = "Se necesitan al menos $requiredTaps toques para activar una emergencia. Detectados: $actualTaps"

        _uiState.value = _uiState.value.copy(
            errorMessage = message
        )

        // Limpiar el mensaje después de 3 segundos
        viewModelScope.launch {
            delay(3000)
            if (_uiState.value.errorMessage == message) {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }
        }

        Log.i("HomeViewModel", "💡 Mensaje mostrado al usuario: $message")
    }

    private fun activateEmergencyWithPriority(priority: String) {
        viewModelScope.launch {
            Log.d("HomeViewModel", "🚨 Activando emergencia con prioridad: $priority")

            // Verificar condiciones básicas
            if (!_isAuthenticated.value) {
                Log.e("HomeViewModel", "❌ No autenticado")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Debes iniciar sesión para activar emergencias"
                )
                return@launch
            }

            val currentLocation = _locationInfo.value
            if (currentLocation == null) {
                Log.e("HomeViewModel", "❌ Sin ubicación")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No se pudo obtener la ubicación. Intenta nuevamente."
                )
                return@launch
            }

            // Verificar contactos
            val contactsResult = contactUseCase.getContactsForEmergency()
            if (contactsResult.isFailure) {
                Log.w("HomeViewModel", "⚠️ ${contactsResult.exceptionOrNull()?.message}")
            }

            // Actualizar estado a loading
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                emergencyStatus = EmergencyStatus.PENDING,
                errorMessage = null
            )

            _panicState.value = PanicState.Sending

            try {
                val locationForEmergency = com.example.panicshield.domain.usecase.LocationInfo(
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    address = currentLocation.address,
                    accuracy = currentLocation.accuracy
                )

                val message = when (priority) {
                    "CRITICAL" -> "🚨 EMERGENCIA CRÍTICA - Múltiples toques rápidos detectados"
                    "HIGH" -> "⚠️ EMERGENCIA MODERADA - Toques rápidos detectados"
                    else -> "Emergencia activada desde botón de pánico"
                }

                // Crear emergencia con prioridad específica
                val result = emergencyUseCase.createPanicAlert(
                    location = locationForEmergency,
                    message = message,
                    priority = priority
                )

                when (result) {
                    is EmergencyResult.Success -> {
                        Log.d("HomeViewModel", "✅ Emergencia $priority creada exitosamente")

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isPanicActivated = true,
                            emergencyId = result.data.id?.toString(),
                            emergencyStatus = EmergencyStatus.ACTIVE,
                            lastPanicTime = System.currentTimeMillis(),
                            connectionStatus = ConnectionStatus.CONNECTED
                        )

                        // Enviar SMS con prioridad específica
                        if (_permissionsGranted.value) {
                            sendSmsAlertsWithPriority(currentLocation, priority)
                        } else {
                            Log.w("HomeViewModel", "⚠️ Sin permisos SMS")
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Emergencia activada. SMS no enviado: permisos requeridos"
                            )
                        }

                        _panicState.value = PanicState.Success
                    }
                    is EmergencyResult.Error -> {
                        Log.e("HomeViewModel", "❌ Error creando emergencia: ${result.exception.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            emergencyStatus = EmergencyStatus.INACTIVE,
                            errorMessage = "Error al crear emergencia: ${result.exception.message}",
                            connectionStatus = ConnectionStatus.ERROR
                        )
                        _panicState.value = PanicState.Error(result.exception.message ?: "Error desconocido")
                    }
                    is EmergencyResult.Loading -> {
                        Log.d("HomeViewModel", "⏳ Emergencia en proceso...")
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Error general: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    emergencyStatus = EmergencyStatus.INACTIVE,
                    errorMessage = "Error: ${e.message}"
                )
                _panicState.value = PanicState.Error(e.message ?: "Error enviando alerta")
            }
        }
    }

    private suspend fun sendSmsAlertsWithPriority(currentLocation: LocationInfo, priority: String) {
        try {
            Log.d("HomeViewModel", "📱 Enviando SMS con prioridad: $priority")

            // Verificaciones básicas
            if (!_permissionsGranted.value) {
                Log.e("HomeViewModel", "❌ Permiso SMS no concedido")
                return
            }

            if (!smsHelper.isConnected()) {
                val connected = smsHelper.connect()
                if (!connected) {
                    Log.e("HomeViewModel", "❌ No se pudo conectar al SMS Manager")
                    return
                }
            }

            val contactsResult = contactUseCase.getContactsForEmergency()

            if (contactsResult.isSuccess) {
                val contacts = contactsResult.getOrNull()
                if (contacts.isNullOrEmpty()) {
                    return
                }

                val priorityText = when (priority) {
                    "CRITICAL" -> "🚨 CRÍTICA"
                    "HIGH" -> "⚠️ MODERADA"
                    else -> "NORMAL"
                }

                val panicAlert = PanicAlert(
                    userPhone = userHelper.getUserPhone(),
                    userName = userHelper.getUserName(),
                    userId = userHelper.getUserId() ?: "ID desconocido",
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    address = currentLocation.address ?: "Ubicación no disponible",
                    priority = priority,
                    emergencyType = "PANIC",
                    message = "🚨 ALERTA DE PÁNICO $priorityText - ${userHelper.getUserName()} necesita ayuda urgente",
                    timestamp = System.currentTimeMillis()
                )

                var successCount = 0
                var failureCount = 0

                contacts.forEach { contact ->
                    try {
                        val smsResult = smsHelper.publishPanicAlert(contact.phone, panicAlert)
                        if (smsResult) {
                            successCount++
                            Log.i("HomeViewModel", "✅ SMS enviado a ${contact.name}")
                        } else {
                            failureCount++
                            Log.e("HomeViewModel", "❌ Fallo SMS a ${contact.name}")
                        }
                    } catch (e: Exception) {
                        failureCount++
                        Log.e("HomeViewModel", "❌ Error SMS a ${contact.name}: ${e.message}")
                    }
                }

                val statusMessage = when {
                    successCount > 0 && failureCount == 0 ->
                        "✅ SMS enviados exitosamente ($successCount)"
                    successCount > 0 && failureCount > 0 ->
                        "⚠️ SMS enviados a $successCount contactos. $failureCount fallaron"
                    else -> "❌ Error enviando SMS a todos los contactos"
                }

                _uiState.value = _uiState.value.copy(smsStatus = statusMessage)

            } else {
                Log.e("HomeViewModel", "❌ Error obteniendo contactos")
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "❌ Error general enviando SMS: ${e.message}", e)
        }
    }

    private fun resetTapState() {
        _tapState.value = TapState()
        Log.d("HomeViewModel", "🔄 Estado de toques reiniciado")
    }

    fun activatePanicWithAlert() {
        handleEmergencyButtonTap()
    }

    // ✅ FUNCIÓN: Enviar SMS usando ContactUseCase
    private suspend fun sendSmsAlerts(currentLocation: LocationInfo) {
        try {
            Log.d("HomeViewModel", "📱 Iniciando envío de SMS a contactos...")

            // ✅ Verificar permiso SMS antes de continuar
            if (!_permissionsGranted.value) {
                Log.e("HomeViewModel", "❌ Permiso SMS no concedido")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Permiso para enviar SMS no concedido"
                )
                return
            }

            // ✅ Verificar conexión del SMS Manager
            if (!smsHelper.isConnected()) {
                Log.w("HomeViewModel", "⚠️ SMS Manager no conectado, intentando reconectar...")
                val connected = smsHelper.connect()
                if (!connected) {
                    Log.e("HomeViewModel", "❌ No se pudo conectar al SMS Manager")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "No se pudo conectar al SMS Manager"
                    )
                    return
                }
                Log.d("HomeViewModel", "✅ SMS Manager reconectado exitosamente")
            }

            // ✅ Obtener contactos específicamente para emergencia
            val contactsResult = contactUseCase.getContactsForEmergency()

            if (contactsResult.isSuccess) {
                val contacts = contactsResult.getOrNull()
                if (contacts.isNullOrEmpty()) {
                    Log.w("HomeViewModel", "⚠️ No hay contactos válidos para SMS")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Emergencia activada. No hay contactos válidos para notificar."
                    )
                    return
                }

                Log.d("HomeViewModel", "📋 Contactos válidos encontrados: ${contacts.size}")

                // ✅ Crear la alerta de pánico
                val panicAlert = PanicAlert(
                    userPhone = userHelper.getUserPhone(),
                    userName = userHelper.getUserName(),
                    userId = userHelper.getUserId() ?: "ID desconocido",
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    address = currentLocation.address ?: "Ubicación no disponible",
                    priority = "HIGH",
                    emergencyType = "PANIC",
                    message = "¡ALERTA DE PÁNICO! ${userHelper.getUserName()} necesita ayuda urgente",
                    timestamp = System.currentTimeMillis()
                )

                var successCount = 0
                var failureCount = 0
                val errors = mutableListOf<String>()
                val successContacts = mutableListOf<String>()

                // ✅ Enviar SMS a cada contacto
                contacts.forEach { contact ->
                    try {
                        Log.d("HomeViewModel", "📱 Enviando SMS a: ${contact.name} (${contact.phone})")

                        val smsResult = smsHelper.publishPanicAlert(
                            contactPhone = contact.phone,
                            panicData = panicAlert
                        )

                        if (smsResult) {
                            successCount++
                            successContacts.add(contact.name)
                            Log.i("HomeViewModel", "✅ SMS enviado exitosamente a ${contact.name}")
                        } else {
                            failureCount++
                            errors.add("Error enviando SMS a ${contact.name}")
                            Log.e("HomeViewModel", "❌ Fallo al enviar SMS a ${contact.name}")
                        }

                    } catch (e: Exception) {
                        failureCount++
                        errors.add("Error con ${contact.name}: ${e.message}")
                        Log.e("HomeViewModel", "❌ Excepción enviando SMS a ${contact.name}: ${e.message}", e)
                    }
                }

                // ✅ Generar mensaje de estado detallado
                val statusMessage = when {
                    successCount > 0 && failureCount == 0 -> {
                        "✅ SMS enviados exitosamente a todos los contactos ($successCount)"
                    }
                    successCount > 0 && failureCount > 0 -> {
                        "⚠️ SMS enviados a $successCount contactos. $failureCount fallaron"
                    }
                    failureCount > 0 -> {
                        "❌ Error enviando SMS a todos los contactos"
                    }
                    else -> "📱 No se enviaron SMS"
                }

                Log.i("HomeViewModel", "📊 Resultado SMS: $successCount exitosos, $failureCount fallidos")
                Log.d("HomeViewModel", "✅ Contactos exitosos: ${successContacts.joinToString(", ")}")

                // ✅ Actualizar estado con resultado
                _uiState.value = _uiState.value.copy(
                    errorMessage = if (errors.isNotEmpty() && successCount == 0) statusMessage else null,
                    smsStatus = statusMessage
                )

                // ✅ Log adicional para debugging
                if (errors.isNotEmpty()) {
                    Log.w("HomeViewModel", "⚠️ Errores durante envío de SMS:")
                    errors.forEach { error ->
                        Log.w("HomeViewModel", "   - $error")
                    }
                }

            } else {
                val errorMessage = contactsResult.exceptionOrNull()?.message ?: "Error desconocido"
                Log.e("HomeViewModel", "❌ Error obteniendo contactos: $errorMessage")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error obteniendo contactos: $errorMessage"
                )
            }

        } catch (e: Exception) {
            Log.e("HomeViewModel", "❌ Error general enviando SMS: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
                errorMessage = "Error enviando SMS: ${e.message}"
            )
        }
    }
    private suspend fun checkContacts() {
        try {
            val contactsResult = contactUseCase.getContacts()
            if (contactsResult.isSuccess) {
                val contacts = contactsResult.getOrNull() ?: emptyList()
                Log.d("HomeViewModel", "🔍 Verificación de contactos:")
                Log.d("HomeViewModel", "   📊 Total contactos: ${contacts.size}")
                Log.d("HomeViewModel", "   👤 Usuario actual: ${userHelper.getUserId()}")

                contacts.forEach { contact ->
                    Log.d("HomeViewModel", "   📱 ${contact.name}: ${contact.phone} (User: ${contact.userId})")
                }
            } else {
                Log.e("HomeViewModel", "❌ Error verificando contactos: ${contactsResult.exceptionOrNull()?.message}")
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "❌ Excepción verificando contactos: ${e.message}", e)
        }
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
                    // Ya manejado arriba
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

    // ✅ FUNCIÓN: Monitoreo de conectividad con suspend
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

    // ✅ FUNCIÓN: Verificar conectividad de red
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
                    // Ya manejado arriba
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

    // ✅ FUNCIÓN: retryConnection
    fun retryConnection() {
        viewModelScope.launch {
            checkNetworkConnectivity()
        }
    }

    // ✅ FUNCIÓN: Detener monitoreo
    fun stopConnectivityMonitoring() {
        _connectivityJob.value?.cancel()
        _connectivityJob.value = null
    }

    // ✅ ACTUALIZAR onCleared
    override fun onCleared() {
        super.onCleared()
        stopConnectivityMonitoring()
    }

    // Estados MQTT
    sealed class PanicState {
        object Idle : PanicState()
        object Sending : PanicState()
        object Success : PanicState()
        data class Error(val message: String) : PanicState()
    }


}