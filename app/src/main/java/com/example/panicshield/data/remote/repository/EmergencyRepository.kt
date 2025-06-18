package com.example.panicshield.data.remote.repository

import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.remote.api.EmergencyApi
import com.example.panicshield.data.remote.dto.CreateEmergencyDto
import com.example.panicshield.data.remote.dto.UpdateEmergencyDto
import com.example.panicshield.domain.model.Emergency
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyRepository @Inject constructor(
    private val emergencyApi: EmergencyApi,
    private val tokenManager: TokenManager
) {

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
                    Emergency(
                        id = dto.id,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        userId = dto.userId ?: userId,
                        emergencyType = dto.emergencyType,
                        status = dto.status,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        address = dto.address,
                        message = dto.message,
                        priority = dto.priority,
                        deviceInfo = dto.deviceInfo,
                        responseTime = dto.responseTime,
                        responderInfo = dto.responderInfo
                    )
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

    //  Obtener todas las emergencias
    suspend fun getAllEmergencies(): Result<List<Emergency>> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val response = emergencyApi.getAllEmergencies(
                authorization = "Bearer $token"
            )

            if (response.isSuccessful) {
                val emergencies = response.body()?.map { dto ->
                    Emergency(
                        id = dto.id,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        userId = dto.userId ?: "", //  Manejar userId nulo
                        emergencyType = dto.emergencyType,
                        status = dto.status,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        address = dto.address,
                        message = dto.message,
                        priority = dto.priority,
                        deviceInfo = dto.deviceInfo,
                        responseTime = dto.responseTime,
                        responderInfo = dto.responderInfo
                    )
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

    //  Crear emergencia
    suspend fun createEmergency(
        emergencyType: String,
        status: String = "active",
        latitude: Double,
        longitude: Double,
        address: String? = null,
        message: String? = null,
        priority: String = "high",
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
                    Emergency(
                        id = dto.id,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        userId = dto.userId ?: userId,
                        emergencyType = dto.emergencyType,
                        status = dto.status,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        address = dto.address,
                        message = dto.message,
                        priority = dto.priority,
                        deviceInfo = dto.deviceInfo,
                        responseTime = dto.responseTime,
                        responderInfo = dto.responderInfo
                    )
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

    //  Actualizar emergencia
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
                id = "eq.$id", //  Formato correcto para Supabase
                emergency = updateEmergencyDto
            )

            if (response.isSuccessful) {
                val updatedEmergency = response.body()?.firstOrNull()?.let { dto ->
                    Emergency(
                        id = dto.id,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        userId = dto.userId ?: "",
                        emergencyType = dto.emergencyType,
                        status = dto.status,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        address = dto.address,
                        message = dto.message,
                        priority = dto.priority,
                        deviceInfo = dto.deviceInfo,
                        responseTime = dto.responseTime,
                        responderInfo = dto.responderInfo
                    )
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

    //  Eliminar emergencia
    suspend fun deleteEmergency(id: Long): Result<Unit> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val response = emergencyApi.deleteEmergency(
                authorization = "Bearer $token",
                id = "eq.$id" //  Formato correcto para Supabase
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

    //  Filtrar emergencias por estado
    suspend fun getEmergenciesByStatus(status: String): Result<List<Emergency>> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val response = emergencyApi.getEmergencies(
                authorization = "Bearer $token",
                status = "eq.$status" //  Formato correcto para Supabase
            )

            if (response.isSuccessful) {
                val emergencies = response.body()?.map { dto ->
                    Emergency(
                        id = dto.id,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        userId = dto.userId ?: "",
                        emergencyType = dto.emergencyType,
                        status = dto.status,
                        latitude = dto.latitude,
                        longitude = dto.longitude,
                        address = dto.address,
                        message = dto.message,
                        priority = dto.priority,
                        deviceInfo = dto.deviceInfo,
                        responseTime = dto.responseTime,
                        responderInfo = dto.responderInfo
                    )
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
}