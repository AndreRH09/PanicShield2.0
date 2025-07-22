package com.example.panicshield.domain.usecase

import com.example.panicshield.data.remote.repository.EmergencyRepository
import com.example.panicshield.data.remote.repository.EmergencyResult
import com.example.panicshield.data.remote.repository.toEmergencyResult

import com.example.panicshield.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject



class EmergencyUseCase @Inject constructor(
    private val emergencyRepository: EmergencyRepository
) {

    suspend fun createPanicAlert(
        location: LocationInfo,
        message: String? = null,
        priority: String = "HIGH"
    ): EmergencyResult<Emergency> {
        return try {
            val deviceInfo = createDeviceInfoMap()

            val result = emergencyRepository.createEmergency(
                emergencyType = EmergencyType.PANIC_BUTTON.toApiValue(),
                status = "pending",
                latitude = location.latitude,
                longitude = location.longitude,
                address = location.address,
                message = message ?: "Emergencia activada desde botón de pánico",
                priority = priority,
                deviceInfo = deviceInfo
            )

            result.toEmergencyResult()
        } catch (e: Exception) {
            EmergencyResult.Error(e)
        }
    }

    suspend fun cancelEmergency(emergencyId: Long): EmergencyResult<Emergency> {
        return try {
            val result = emergencyRepository.cancelEmergency(emergencyId)
            result.toEmergencyResult()
        } catch (e: Exception) {
            EmergencyResult.Error(e)
        }
    }

    suspend fun getCurrentActiveEmergency(): EmergencyResult<Emergency?> {
        return try {
            val result = emergencyRepository.getCurrentActiveEmergency()
            result.toEmergencyResult()
        } catch (e: Exception) {
            EmergencyResult.Error(e)
        }
    }

    suspend fun updateEmergencyStatus(
        emergencyId: Long,
        newStatus: EmergencyStatus
    ): EmergencyResult<Emergency> {
        return try {
            val result = emergencyRepository.updateEmergencyStatus(emergencyId, newStatus)
            result.toEmergencyResult()
        } catch (e: Exception) {
            EmergencyResult.Error(e)
        }
    }

    suspend fun testConnection(): EmergencyResult<Boolean> {
        return try {
            val result = emergencyRepository.hasActiveEmergency()
            when {
                result.isSuccess -> EmergencyResult.Success(true)
                else -> EmergencyResult.Error(result.exceptionOrNull() ?: Exception("Connection failed"))
            }
        } catch (e: Exception) {
            EmergencyResult.Error(e)
        }
    }


    // ===== FUNCIONES DE FLOW =====

    fun isPanicActive(): Flow<Boolean> {
        return emergencyRepository.isPanicActive()
    }

    fun getCurrentEmergency(): Flow<Emergency?> {
        return emergencyRepository.getCurrentEmergencyFlow()
    }

    // ===== FUNCIONES HELPER PRIVADAS =====

    private fun createDeviceInfoMap(): Map<String, Any> {
        return mapOf(
            "model" to android.os.Build.MODEL,
            "androidVersion" to android.os.Build.VERSION.RELEASE,
            "appVersion" to "1.0",
            "timestamp" to System.currentTimeMillis()
        )
    }
}

// Data class para LocationInfo (si no existe)
data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val accuracy: Float? = null
)