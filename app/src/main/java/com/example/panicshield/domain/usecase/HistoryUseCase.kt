package com.example.panicshield.domain.usecase

import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.local.dao.EmergencyHistoryDao
import com.example.panicshield.data.remote.repository.EmergencyRepository
import com.example.panicshield.data.sync.SimpleSyncManager
import com.example.panicshield.domain.mapper.toCacheEntity
import com.example.panicshield.domain.mapper.toEmergencyHistory
import com.example.panicshield.domain.model.*
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

// EmergencyResult para compatibilidad (duplicamos aquí para que sea self-contained)
sealed class EmergencyResult<out T> {
    data class Success<out T>(val data: T) : EmergencyResult<T>()
    data class Error(val exception: Throwable, val code: Int? = null) : EmergencyResult<Nothing>()
    object Loading : EmergencyResult<Nothing>()
}



@Singleton
class HistoryUseCase @Inject constructor(
    private val emergencyRepository: EmergencyRepository,
    private val localDao: EmergencyHistoryDao, // NUEVO
    private val syncManager: SimpleSyncManager, // NUEVO
    private val tokenManager: TokenManager // NUEVO

) {

    suspend fun getEmergencyHistory(): EmergencyResult<List<EmergencyHistory>> {
        return try {
            val userId = tokenManager.getUserId().first() ?: ""

            // 1. Intentar sincronizar si hay conexión
            syncManager.syncIfConnected()

            // 2. Obtener datos del cache local
            val localData = localDao.getHistoryByUser(userId)

            if (localData.isNotEmpty()) {
                // Devolver datos del cache
                val history = localData.map { it.toEmergencyHistory() }
                    .sortedByDescending { it.createdAt }

                EmergencyResult.Success(history)
            } else {
                // Si no hay cache, intentar API directamente
                val result = emergencyRepository.getEmergencyHistory()

                when {
                    result.isSuccess -> {
                        val emergencies = result.getOrNull() ?: emptyList()
                        val history = emergencies.map { emergency ->
                            EmergencyHistory(
                                id = emergency.id,
                                userId = emergency.userId,
                                emergencyType = emergency.emergencyType,
                                status = emergency.statusEnum,
                                latitude = emergency.latitude ?: 0.0,
                                longitude = emergency.longitude ?: 0.0,
                                address = emergency.address,
                                message = emergency.message,
                                createdAt = parseTimestamp(emergency.createdAt),
                                updatedAt = emergency.updatedAt?.let { parseTimestamp(it) },
                                deviceInfo = emergency.deviceInfo?.let { convertMapToString(it) },
                                priority = emergency.priority,
                                responseTime = emergency.responseTime
                            )
                        }.sortedByDescending { it.createdAt }

                        // Guardar en cache para próxima vez
                        val cacheEntities = emergencies.map { it.toCacheEntity() }
                        localDao.insertAll(cacheEntities)

                        EmergencyResult.Success(history)
                    }
                    else -> {
                        EmergencyResult.Error(result.exceptionOrNull() ?: Exception("Unknown error"))
                    }
                }
            }
        } catch (e: Exception) {
            EmergencyResult.Error(e)
        }
    }

    suspend fun getEmergencyById(emergencyId: Long): EmergencyResult<EmergencyHistory?> {
        return try {
            val result = emergencyRepository.getEmergencyById(emergencyId)

            when {
                result.isSuccess -> {
                    val emergency = result.getOrNull()
                    if (emergency != null) {
                        val history = EmergencyHistory(
                            id = emergency.id,
                            userId = emergency.userId,
                            emergencyType = emergency.emergencyType,
                            status = emergency.statusEnum,
                            latitude = emergency.latitude ?: 0.0,
                            longitude = emergency.longitude ?: 0.0,
                            address = emergency.address,
                            message = emergency.message,
                            createdAt = parseTimestamp(emergency.createdAt),
                            updatedAt = emergency.updatedAt?.let { parseTimestamp(it) },
                            deviceInfo = emergency.deviceInfo?.let { convertMapToString(it) },
                            priority = emergency.priority,
                            responseTime = emergency.responseTime
                        )
                        EmergencyResult.Success(history)
                    } else {
                        EmergencyResult.Success(null)
                    }
                }
                else -> {
                    EmergencyResult.Error(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            }
        } catch (e: Exception) {
            EmergencyResult.Error(e)
        }
    }

    suspend fun getEmergencyHistoryByDateRange(
        startDate: Long,
        endDate: Long
    ): EmergencyResult<List<EmergencyHistory>> {
        return when (val result = getEmergencyHistory()) {
            is EmergencyResult.Success -> {
                val filtered = result.data.filter { emergency ->
                    emergency.createdAt >= startDate && emergency.createdAt <= endDate
                }
                EmergencyResult.Success(filtered)
            }
            is EmergencyResult.Error -> result
            is EmergencyResult.Loading -> EmergencyResult.Loading
        }
    }

    suspend fun getEmergencyHistoryByStatus(
        status: EmergencyStatus
    ): EmergencyResult<List<EmergencyHistory>> {
        return when (val result = getEmergencyHistory()) {
            is EmergencyResult.Success -> {
                val filtered = result.data.filter { emergency ->
                    emergency.status == status
                }
                EmergencyResult.Success(filtered)
            }
            is EmergencyResult.Error -> result
            is EmergencyResult.Loading -> EmergencyResult.Loading
        }
    }

    suspend fun getEmergencyStatistics(): EmergencyResult<EmergencyStatistics> {
        return when (val result = getEmergencyHistory()) {
            is EmergencyResult.Success -> {
                val emergencies = result.data

                val statistics = EmergencyStatistics(
                    totalEmergencies = emergencies.size,
                    activeEmergencies = emergencies.count { it.status == EmergencyStatus.ACTIVE },
                    completedEmergencies = emergencies.count { it.status == EmergencyStatus.COMPLETED },
                    cancelledEmergencies = emergencies.count { it.status == EmergencyStatus.CANCELLED },
                    averageResponseTime = calculateAverageResponseTime(emergencies),
                    thisWeekCount = getThisWeekCount(emergencies),
                    thisMonthCount = getThisMonthCount(emergencies),
                    mostCommonType = getMostCommonEmergencyType(emergencies)
                )

                EmergencyResult.Success(statistics)
            }
            is EmergencyResult.Error -> result
            is EmergencyResult.Loading -> EmergencyResult.Loading
        }
    }

    suspend fun searchEmergencyHistory(query: String): EmergencyResult<List<EmergencyHistory>> {
        return when (val result = getEmergencyHistory()) {
            is EmergencyResult.Success -> {
                val searchQuery = query.lowercase().trim()
                val filtered = result.data.filter { emergency ->
                    emergency.emergencyType.lowercase().contains(searchQuery) ||
                            emergency.message?.lowercase()?.contains(searchQuery) == true ||
                            emergency.address?.lowercase()?.contains(searchQuery) == true ||
                            emergency.status.name.lowercase().contains(searchQuery)
                }
                EmergencyResult.Success(filtered)
            }
            is EmergencyResult.Error -> result
            is EmergencyResult.Loading -> EmergencyResult.Loading
        }
    }

    // ===== FUNCIONES HELPER PRIVADAS =====

    private fun parseTimestamp(timestamp: String?): Long {
        return try {
            if (timestamp.isNullOrBlank()) {
                return System.currentTimeMillis()
            }

            // Formato esperado: 2025-06-17T04:49:05.348404+00:00
            val cleanTimestamp = timestamp
                .replace("T", " ")           // 2025-06-17 04:49:05.348404+00:00
                .split(".")[0]               // 2025-06-17 04:49:05
                .split("+")[0]               // 2025-06-17 04:49:05
                .trim()

            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")

            val date = formatter.parse(cleanTimestamp)
            date?.time ?: System.currentTimeMillis()

        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun convertMapToString(map: Map<String, Any>): String {
        return try {
            map.entries.joinToString(", ") { "${it.key}: ${it.value}" }
        } catch (e: Exception) {
            "Unknown device info"
        }
    }

    private fun formatDebugDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    private fun calculateAverageResponseTime(emergencies: List<EmergencyHistory>): Long {
        val responseTimes = emergencies.mapNotNull { it.responseTime }
        return if (responseTimes.isNotEmpty()) {
            responseTimes.average().toLong()
        } else {
            0L
        }
    }

    private fun getThisWeekCount(emergencies: List<EmergencyHistory>): Int {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        return emergencies.count { emergency ->
            calendar.timeInMillis = emergency.createdAt
            calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek &&
                    calendar.get(Calendar.YEAR) == currentYear
        }
    }

    private fun getThisMonthCount(emergencies: List<EmergencyHistory>): Int {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        return emergencies.count { emergency ->
            calendar.timeInMillis = emergency.createdAt
            calendar.get(Calendar.MONTH) == currentMonth &&
                    calendar.get(Calendar.YEAR) == currentYear
        }
    }

    private fun getMostCommonEmergencyType(emergencies: List<EmergencyHistory>): String {
        return emergencies
            .groupBy { it.emergencyType }
            .maxByOrNull { it.value.size }
            ?.key ?: "panic_button"
    }
}

// ===== DATA CLASSES AUXILIARES =====

data class EmergencyHistory(
    val id: Long?,
    val userId: String,
    val emergencyType: String,
    val status: EmergencyStatus,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val message: String?,
    val createdAt: Long,
    val updatedAt: Long?,
    val deviceInfo: String?,
    val priority: String?,
    val responseTime: Int?
)

data class EmergencyStatistics(
    val totalEmergencies: Int,
    val activeEmergencies: Int,
    val completedEmergencies: Int,
    val cancelledEmergencies: Int,
    val averageResponseTime: Long,
    val thisWeekCount: Int,
    val thisMonthCount: Int,
    val mostCommonType: String
)