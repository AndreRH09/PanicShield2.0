package com.example.panicshield.domain.usecase

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import com.example.panicshield.domain.model.LocationInfo
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _currentLocation = MutableStateFlow<LocationInfo?>(null)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // ✅ SOLUCION: Solo una función pública, sin propiedad conflictiva
    fun getCurrentLocation(): Flow<LocationInfo?> {
        return _currentLocation.asStateFlow()
    }

    @SuppressLint("MissingPermission")
    suspend fun updateLocation() {
        try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                _currentLocation.value = LocationInfo(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracy = it.accuracy,
                    timestamp = it.time,
                    isLocationActive = true
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun requestLocationPermission(): Boolean {
        // TODO: Implementar lógica de permisos
        return true
    }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun clearLocation() {
        _currentLocation.value = null
    }
}