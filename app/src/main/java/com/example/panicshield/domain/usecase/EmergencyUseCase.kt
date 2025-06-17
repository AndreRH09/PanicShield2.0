package com.example.panicshield.domain.usecase

import com.example.panicshield.domain.mapper.toDomainModel
import com.example.panicshield.data.repository.EmergencyRepository
import com.example.panicshield.data.repository.EmergencyResult
import com.example.panicshield.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EmergencyUseCase @Inject constructor(
    private val emergencyRepository: EmergencyRepository
) {

    suspend fun createPanicAlert(
        authToken: String,
        userId: String,
        location: LocationInfo,
        message: String? = null
    ): EmergencyResult<Emergency> {
        val deviceInfo = createDeviceInfoJson()

        return when (val result = emergencyRepository.createEmergency(
            authToken = authToken,
            userId = userId,
            emergencyType = EmergencyType.PANIC_BUTTON.toApiValue(),
            latitude = location.latitude,
            longitude = location.longitude,
            address = location.address,
            message = message ?: "Emergencia activada desde botón de pánico",
            deviceInfo = deviceInfo
        )) {
            is EmergencyResult.Success -> {
                EmergencyResult.Success(result.data.toDomainModel())
            }
            is EmergencyResult.Error -> result
            is EmergencyResult.Loading -> result
        }
    }

    suspend fun cancelEmergency(
        authToken: String,
        emergencyId: Long
    ): EmergencyResult<Emergency> {
        return when (val result = emergencyRepository.cancelEmergency(authToken, emergencyId)) {
            is EmergencyResult.Success -> {
                EmergencyResult.Success(result.data.toDomainModel())
            }
            is EmergencyResult.Error -> result
            is EmergencyResult.Loading -> result
        }
    }

    suspend fun getCurrentActiveEmergency(
        authToken: String,
        userId: String
    ): EmergencyResult<Emergency?> {
        return when (val result = emergencyRepository.getCurrentActiveEmergency(authToken, userId)) {
            is EmergencyResult.Success -> {
                EmergencyResult.Success(result.data?.toDomainModel())
            }
            is EmergencyResult.Error -> result
            is EmergencyResult.Loading -> result
        }
    }

    suspend fun updateEmergencyStatus(
        authToken: String,
        emergencyId: Long,
        newStatus: EmergencyStatus
    ): EmergencyResult<Emergency> {
        return when (val result = emergencyRepository.updateEmergencyStatus(
            authToken, emergencyId, newStatus.toApiValue()
        )) {
            is EmergencyResult.Success -> {
                EmergencyResult.Success(result.data.toDomainModel())
            }
            is EmergencyResult.Error -> result
            is EmergencyResult.Loading -> result
        }
    }
    suspend fun testConnection(
        authToken: String,
        userId: String
    ): EmergencyResult<Boolean> {
        return try {
            // Hace una llamada simple para verificar conectividad
            when (val result = emergencyRepository.getCurrentActiveEmergency(authToken, userId)) {
                is EmergencyResult.Success -> {
                    EmergencyResult.Success(true)
                }
                is EmergencyResult.Error -> {
                    // Propagar el error con código si existe
                    EmergencyResult.Error(result.exception, result.code)
                }
                is EmergencyResult.Loading -> {
                    EmergencyResult.Loading
                }
            }
        } catch (e: Exception) {
            EmergencyResult.Error(e)
        }
    }

    fun isPanicActive(): Flow<Boolean> {
        return emergencyRepository.isPanicActive()
    }

    fun getCurrentEmergency(): Flow<Emergency?> {
        return emergencyRepository.currentEmergency.map { dto ->
            dto?.toDomainModel()
        }
    }


    private fun createDeviceInfoJson(): String {
        return """
            {
                "model": "${android.os.Build.MODEL}",
                "android_version": "${android.os.Build.VERSION.RELEASE}",
                "app_version": "1.0",
                "timestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent()
    }
}