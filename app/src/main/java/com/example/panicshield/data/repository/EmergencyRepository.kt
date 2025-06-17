package com.example.panicshield.data.repository

import com.example.panicshield.data.remote.api.EmergencyApi
import com.example.panicshield.data.remote.dto.*
import kotlinx.coroutines.flow.*
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

sealed class EmergencyResult<out T> {
    data class Success<out T>(val data: T) : EmergencyResult<T>()
    data class Error(val exception: Throwable, val code: Int? = null) : EmergencyResult<Nothing>()
    object Loading : EmergencyResult<Nothing>()
}

@Singleton
class EmergencyRepository @Inject constructor(
    private val emergencyApi: EmergencyApi
) {

    private val _currentEmergency = MutableStateFlow<EmergencyDto?>(null)
    val currentEmergency: StateFlow<EmergencyDto?> = _currentEmergency.asStateFlow()

    suspend fun createEmergency(
        authToken: String,
        userId: String,
        emergencyType: String,
        latitude: Double,
        longitude: Double,
        address: String? = null,
        message: String? = null,
        deviceInfo: String? = null
    ): EmergencyResult<EmergencyDto> {
        return try {
            val createDto = CreateEmergencyDto(
                userId = userId,
                emergencyType = emergencyType,
                latitude = latitude,
                longitude = longitude,
                address = address,
                message = message,
                deviceInfo = deviceInfo
            )

            val response = emergencyApi.createEmergency(
                authorization = "Bearer $authToken",
                emergency = createDto
            )

            if (response.isSuccessful && response.body() != null) {
                val emergency = response.body()!!.firstOrNull()
                if (emergency != null) {
                    _currentEmergency.value = emergency
                    EmergencyResult.Success(emergency)
                } else {
                    EmergencyResult.Error(
                        exception = Exception("No se pudo crear la emergencia - respuesta vacía"),
                        code = response.code()
                    )                }
            } else {
                val errorBody = response.errorBody()?.string()
                EmergencyResult.Error(
                    exception = Exception("HTTP ${response.code()}: $errorBody"),
                    code = response.code() // ✅ INCLUIR CÓDIGO HTTP
                )
            }
        } catch (e: Exception) {
            EmergencyResult.Error(e)
        }
    }

    suspend fun getCurrentActiveEmergency(
        authToken: String,
        userId: String
    ): EmergencyResult<EmergencyDto?> {
        return try {
            val response = emergencyApi.getCurrentActiveEmergency(
                authorization = "Bearer $authToken",
                userId = "eq.$userId"
            )

            if (response.isSuccessful) {
                val emergency = response.body()?.firstOrNull()
                _currentEmergency.value = emergency
                EmergencyResult.Success(emergency)
            } else {
                val errorBody = response.errorBody()?.string()
                EmergencyResult.Error(
                    exception = Exception("HTTP ${response.code()}: $errorBody"),
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            EmergencyResult.Error(e, null)
        }
    }

    suspend fun updateEmergencyStatus(
        authToken: String,
        emergencyId: Long,
        newStatus: String
    ): EmergencyResult<EmergencyDto> {
        return try {
            val updateDto = UpdateEmergencyDto(status = newStatus)

            val response = emergencyApi.updateEmergency(
                authorization = "Bearer $authToken",
                id = "eq.$emergencyId",
                emergency = updateDto
            )

            handleApiResponse(response) { emergencies ->
                val emergency = emergencies.firstOrNull()
                if (emergency != null) {
                    _currentEmergency.value = emergency
                    emergency
                } else {
                    throw Exception("No se pudo actualizar la emergencia")
                }
            }
        } catch (e: Exception) {
            EmergencyResult.Error(e)
        }
    }

    suspend fun cancelEmergency(
        authToken: String,
        emergencyId: Long
    ): EmergencyResult<EmergencyDto> {
        return updateEmergencyStatus(authToken, emergencyId, "cancelled")
    }

    suspend fun hasActiveEmergency(
        authToken: String,
        userId: String
    ): EmergencyResult<Boolean> {
        return try {
            val response = emergencyApi.hasActiveEmergency(
                authorization = "Bearer $authToken",
                userId = "eq.$userId"
            )

            handleApiResponse(response) { result ->
                result.isNotEmpty()
            }
        } catch (e: Exception) {
            EmergencyResult.Error(e)
        }
    }

    fun isPanicActive(): Flow<Boolean> {
        return currentEmergency.map { emergency ->
            emergency?.status in listOf("pending", "active")
        }
    }

    private fun <T, R> handleApiResponse(
        response: Response<T>,
        transform: (T) -> R
    ): EmergencyResult<R> {
        return if (response.isSuccessful && response.body() != null) {
            try {
                val result = transform(response.body()!!)
                EmergencyResult.Success(result)
            } catch (e: Exception) {
                EmergencyResult.Error(e)
            }
        } else {
            EmergencyResult.Error(
                exception = Exception("API Error: ${response.message()}"),
                code = response.code()
            )
        }
    }

    // ✅ FUNCIÓN: Obtener historial completo de emergencias
    suspend fun getEmergencyHistory(
        authToken: String,
        userId: String
    ): EmergencyResult<List<EmergencyDto>> {
        return try {
            val response = emergencyApi.getEmergencyHistory(
                authorization = "Bearer $authToken",
                userId = "eq.$userId"
            )

            if (response.isSuccessful && response.body() != null) {
                val emergencies = response.body()!!
                EmergencyResult.Success(emergencies)
            } else {
                val errorBody = response.errorBody()?.string()
                EmergencyResult.Error(
                    exception = Exception("HTTP ${response.code()}: $errorBody"),
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            EmergencyResult.Error(e, null)
        }
    }

    // ✅ FUNCIÓN: Obtener emergencia por ID
    suspend fun getEmergencyById(
        authToken: String,
        emergencyId: Long
    ): EmergencyResult<EmergencyDto?> {
        return try {
            val response = emergencyApi.getEmergencyById(
                authorization = "Bearer $authToken",
                id = "eq.$emergencyId"
            )

            if (response.isSuccessful && response.body() != null) {
                val emergency = response.body()!!.firstOrNull()
                EmergencyResult.Success(emergency)
            } else {
                val errorBody = response.errorBody()?.string()
                EmergencyResult.Error(
                    exception = Exception("HTTP ${response.code()}: $errorBody"),
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            EmergencyResult.Error(e, null)
        }
    }




}