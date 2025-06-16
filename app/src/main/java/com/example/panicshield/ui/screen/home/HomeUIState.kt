package com.example.panicshield.ui.screen.home

import com.example.panicshield.domain.model.EmergencyStatus

/**
 * Estado de la UI para la pantalla Home
 * Esta es una data class simple, SIN delegados de Compose
 */
data class HomeUIState(
    val isPanicActivated: Boolean = false,
    val isLoading: Boolean = false,
    val emergencyStatus: EmergencyStatus = EmergencyStatus.INACTIVE,
    val emergencyId: String? = null,
    val isLocationPermissionGranted: Boolean = false,
    val networkAvailable: Boolean = true,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val errorMessage: String? = null,
    val lastPanicTime: Long? = null,
    val lastSyncTime: Long? = null
)

enum class ConnectionStatus {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    ERROR
}