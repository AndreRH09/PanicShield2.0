package com.example.panicshield.domain.usecase

import com.example.panicshield.domain.model.LocationInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationUseCase @Inject constructor(
    // Aquí inyectarías tu LocationRepository cuando lo tengas
) {

    private val _currentLocation = MutableStateFlow<LocationInfo?>(null)

    // ✅ SOLUCION: Solo una función pública, sin propiedad conflictiva
    fun getCurrentLocation(): Flow<LocationInfo?> {
        return _currentLocation.asStateFlow()
    }

    suspend fun updateLocation() {
        // TODO: Implementar obtención de ubicación real
        // Por ahora devolvemos una ubicación mock para Lima, Perú
        _currentLocation.value = LocationInfo(
            latitude = -12.0464,
            longitude = -77.0428,
            accuracy = 10.0f,
            address = "Lima, Perú",
            timestamp = System.currentTimeMillis(),
            isLocationActive = true
        )
    }

    suspend fun requestLocationPermission(): Boolean {
        // TODO: Implementar lógica de permisos
        return true
    }

    fun isLocationEnabled(): Boolean {
        // TODO: Implementar verificación de GPS
        return true
    }

    fun clearLocation() {
        _currentLocation.value = null
    }
}