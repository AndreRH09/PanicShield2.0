package com.example.panicshield.data.repository

import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.remote.api.EmergencyApi
import com.example.panicshield.data.remote.dto.CreateEmergencyDto
import com.example.panicshield.data.remote.dto.UpdateEmergencyDto
import com.example.panicshield.data.remote.dto.EmergencyDto
import com.example.panicshield.domain.model.*
import com.example.panicshield.domain.mapper.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton


// EmergencyResult para compatibilidad con código legacy
sealed class EmergencyResult<out T> {
    data class Success<out T>(val data: T) : EmergencyResult<T>()
    data class Error(val exception: Throwable, val code: Int? = null) : EmergencyResult<Nothing>()
    object Loading : EmergencyResult<Nothing>()
}

// Extensión para convertir Result<T> a EmergencyResult<T>
fun <T> Result<T>.toEmergencyResult(): EmergencyResult<T> {
    return fold(
        onSuccess = { EmergencyResult.Success(it) },
        onFailure = { EmergencyResult.Error(it) }
    )
}


@Singleton
class EmergencyRepository @Inject constructor(
    private val emergencyApi: EmergencyApi,
    private val tokenManager: TokenManager
) {

    // Estado actual de emergencia para tracking
    private val _currentEmergency = MutableStateFlow<EmergencyDto?>(null)
    val currentEmergency: StateFlow<EmergencyDto?> = _currentEmergency.asStateFlow()

    // ===== FUNCIONES PRINCIPALES =====

    suspend fun getUserEmergencies(): Result<List<Emergency>> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val userId = tokenManager.getUserId().first()
                ?: return Result.failure(Exception("No user ID available"))

            val response = emergencyApi.getEmergencies(
                authorization = "Bearer $token",
                userId = "eq.$userId"
            )

            if (response.isSuccessful) {
                val emergencies = response.body()?.map { dto ->
                    dto.toDomainModel()
                } ?: emptyList()
                Result.success(emergencies)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllEmergencies(): Result<List<Emergency>> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val response = emergencyApi.getAllEmergencies(
                authorization = "Bearer $token"
            )

            if (response.isSuccessful) {
                val emergencies = response.body()?.map { dto ->
                    dto.toDomainModel()
                } ?: emptyList()
                Result.success(emergencies)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Versión con tipos enumerados
    suspend fun createEmergency(
        emergencyType: EmergencyType,
        status: EmergencyStatus = EmergencyStatus.PENDING,
        location: EmergencyLocation,
        message: String? = null,
        priority: EmergencyPriority = EmergencyPriority.HIGH,
        deviceInfo: DeviceInfo? = null
    ): Result<Emergency> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val userId = tokenManager.getUserId().first()
                ?: return Result.failure(Exception("No user ID available"))

            val createEmergencyDto = createEmergencyDto(
                userId = userId,
                emergencyType = emergencyType,
                status = status,
                location = location,
                message = message,
                priority = priority,
                deviceInfo = deviceInfo
            )

            val response = emergencyApi.createEmergency(
                authorization = "Bearer $token",
                emergency = createEmergencyDto
            )

            if (response.isSuccessful) {
                val createdEmergency = response.body()?.firstOrNull()?.let { dto ->
                    _currentEmergency.value = dto
                    dto.toDomainModel()
                } ?: return Result.failure(Exception("Failed to create emergency"))

                Result.success(createdEmergency)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Versión con parámetros simples (compatibilidad)
    suspend fun createEmergency(
        emergencyType: String,
        status: String = "pending",
        latitude: Double,
        longitude: Double,
        address: String? = null,
        message: String? = null,
        priority: String = "HIGH",
        deviceInfo: Map<String, Any>? = null
    ): Result<Emergency> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val userId = tokenManager.getUserId().first()
                ?: return Result.failure(Exception("No user ID available"))

            val createEmergencyDto = CreateEmergencyDto(
                userId = userId,
                emergencyType = emergencyType,
                status = status,
                latitude = latitude,
                longitude = longitude,
                address = address,
                message = message,
                priority = priority,
                deviceInfo = deviceInfo
            )

            val response = emergencyApi.createEmergency(
                authorization = "Bearer $token",
                emergency = createEmergencyDto
            )

            if (response.isSuccessful) {
                val createdEmergency = response.body()?.firstOrNull()?.let { dto ->
                    _currentEmergency.value = dto
                    dto.toDomainModel()
                } ?: return Result.failure(Exception("Failed to create emergency"))

                Result.success(createdEmergency)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Versión con tipos enumerados
    suspend fun updateEmergency(
        id: Long,
        emergencyType: EmergencyType? = null,
        status: EmergencyStatus? = null,
        location: EmergencyLocation? = null,
        message: String? = null,
        priority: EmergencyPriority? = null,
        deviceInfo: DeviceInfo? = null,
        responseTime: Int? = null,
        responderInfo: ResponderInfo? = null
    ): Result<Emergency> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val updateEmergencyDto = createUpdateEmergencyDto(
                emergencyType = emergencyType,
                status = status,
                location = location,
                message = message,
                priority = priority,
                deviceInfo = deviceInfo,
                responseTime = responseTime,
                responderInfo = responderInfo
            )

            val response = emergencyApi.updateEmergency(
                authorization = "Bearer $token",
                id = "eq.$id",
                emergency = updateEmergencyDto
            )

            if (response.isSuccessful) {
                val updatedEmergency = response.body()?.firstOrNull()?.let { dto ->
                    _currentEmergency.value = dto
                    dto.toDomainModel()
                } ?: return Result.failure(Exception("Failed to update emergency"))

                Result.success(updatedEmergency)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Versión con parámetros simples (compatibilidad)
    suspend fun updateEmergency(
        id: Long,
        emergencyType: String? = null,
        status: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        address: String? = null,
        message: String? = null,
        priority: String? = null,
        deviceInfo: Map<String, Any>? = null,
        responseTime: Int? = null,
        responderInfo: Map<String, Any>? = null
    ): Result<Emergency> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val updateEmergencyDto = UpdateEmergencyDto(
                emergencyType = emergencyType,
                status = status,
                latitude = latitude,
                longitude = longitude,
                address = address,
                message = message,
                priority = priority,
                deviceInfo = deviceInfo,
                responseTime = responseTime,
                responderInfo = responderInfo
            )

            val response = emergencyApi.updateEmergency(
                authorization = "Bearer $token",
                id = "eq.$id",
                emergency = updateEmergencyDto
            )

            if (response.isSuccessful) {
                val updatedEmergency = response.body()?.firstOrNull()?.let { dto ->
                    _currentEmergency.value = dto
                    dto.toDomainModel()
                } ?: return Result.failure(Exception("Failed to update emergency"))

                Result.success(updatedEmergency)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEmergency(id: Long): Result<Unit> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val response = emergencyApi.deleteEmergency(
                authorization = "Bearer $token",
                id = "eq.$id"
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEmergenciesByStatus(status: EmergencyStatus): Result<List<Emergency>> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val response = emergencyApi.getEmergencies(
                authorization = "Bearer $token",
                status = "eq.${status.toApiValue()}"
            )

            if (response.isSuccessful) {
                val emergencies = response.body()?.map { dto ->
                    dto.toDomainModel()
                } ?: emptyList()
                Result.success(emergencies)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sobrecarga para compatibilidad con String
    suspend fun getEmergenciesByStatus(status: String): Result<List<Emergency>> {
        return getEmergenciesByStatus(EmergencyStatus.fromApiValue(status))
    }

    // ===== FUNCIONES ESPECÍFICAS ADICIONALES =====

    suspend fun getCurrentActiveEmergency(): Result<Emergency?> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val userId = tokenManager.getUserId().first()
                ?: return Result.failure(Exception("No user ID available"))

            val response = emergencyApi.getCurrentActiveEmergency(
                authorization = "Bearer $token",
                userId = "eq.$userId"
            )

            if (response.isSuccessful) {
                val emergency = response.body()?.firstOrNull()?.let { dto ->
                    _currentEmergency.value = dto
                    dto.toDomainModel()
                }
                Result.success(emergency)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEmergencyById(id: Long): Result<Emergency?> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val response = emergencyApi.getEmergencyById(
                authorization = "Bearer $token",
                id = "eq.$id"
            )

            if (response.isSuccessful) {
                val emergency = response.body()?.firstOrNull()?.let { dto ->
                    dto.toDomainModel()
                }
                Result.success(emergency)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEmergencyHistory(): Result<List<Emergency>> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val userId = tokenManager.getUserId().first()
                ?: return Result.failure(Exception("No user ID available"))

            val response = emergencyApi.getEmergencyHistory(
                authorization = "Bearer $token",
                userId = "eq.$userId"
            )

            if (response.isSuccessful) {
                val emergencies = response.body()?.map { dto ->
                    dto.toDomainModel()
                } ?: emptyList()
                Result.success(emergencies)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasActiveEmergency(): Result<Boolean> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val userId = tokenManager.getUserId().first()
                ?: return Result.failure(Exception("No user ID available"))

            val response = emergencyApi.hasActiveEmergency(
                authorization = "Bearer $token",
                userId = "eq.$userId"
            )

            if (response.isSuccessful) {
                val hasActive = response.body()?.isNotEmpty() ?: false
                Result.success(hasActive)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelEmergency(id: Long): Result<Emergency> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val response = emergencyApi.cancelEmergency(
                authorization = "Bearer $token",
                id = "eq.$id"
            )

            if (response.isSuccessful) {
                val cancelledEmergency = response.body()?.firstOrNull()?.let { dto ->
                    _currentEmergency.value = dto
                    dto.toDomainModel()
                } ?: return Result.failure(Exception("Failed to cancel emergency"))

                Result.success(cancelledEmergency)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}. Body: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== FUNCIONES DE ESTADO Y FLOWS =====

    fun isPanicActive(): Flow<Boolean> {
        return currentEmergency.map { emergency ->
            emergency?.status in listOf("pending", "active")
        }
    }

    fun getCurrentEmergencyFlow(): Flow<Emergency?> {
        return currentEmergency.map { dto ->
            dto?.toDomainModel()
        }
    }

    // ===== FUNCIONES LEGACY PARA COMPATIBILIDAD =====

    suspend fun updateEmergencyStatus(id: Long, status: String): Result<Emergency> {
        return updateEmergency(id = id, status = status)
    }

    suspend fun updateEmergencyStatus(id: Long, status: EmergencyStatus): Result<Emergency> {
        return updateEmergency(id = id, status = status)
    }
}