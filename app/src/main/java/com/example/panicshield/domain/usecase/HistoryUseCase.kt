package com.example.panicshield.domain.usecase

import com.example.panicshield.data.repository.EmergencyRepository
import com.example.panicshield.data.repository.EmergencyResult
import com.example.panicshield.domain.model.EmergencyHistory
import com.example.panicshield.domain.model.EmergencyStatus
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryUseCase @Inject constructor(
    private val emergencyRepository: EmergencyRepository
) {

    // ✅ FUNCIÓN: Obtener historial completo de emergencias
    suspend fun getEmergencyHistory(
        authToken: String,
        userId: String
    ): EmergencyResult<List<EmergencyHistory>> {
        return when (val result = emergencyRepository.getEmergencyHistory(authToken, userId)) {
            is EmergencyResult.Success -> {
                // ✅ DEBUGGING: Imprimir datos recibidos de la API
                println("🔍 DEBUG: Datos recibidos de API:")
                result.data.forEachIndexed { index, dto ->
                    println("[$index] ID: ${dto.id}")
                    println("[$index] Created: '${dto.createdAt}'")
                    println("[$index] Updated: '${dto.updatedAt}'")
                    println("[$index] Type: '${dto.emergencyType}'")
                    println("[$index] Status: '${dto.status}'")
                    println("---")
                }

                val history = result.data.map { emergencyDto ->
                    // ✅ DEBUGGING: Mostrar proceso de parsing
                    val createdTimestamp = parseTimestamp(emergencyDto.createdAt)
                    val updatedTimestamp = emergencyDto.updatedAt?.let { parseTimestamp(it) }

                    println("🔄 PARSING:")
                    println("Original created_at: '${emergencyDto.createdAt}'")
                    println("Parsed timestamp: $createdTimestamp")
                    println("Formatted date: ${formatDebugDate(createdTimestamp)}")
                    println("---")

                    EmergencyHistory(
                        id = emergencyDto.id,
                        userId = emergencyDto.userId,
                        emergencyType = emergencyDto.emergencyType ?: "panic_button",
                        status = parseEmergencyStatus(emergencyDto.status),
                        latitude = emergencyDto.latitude ?: 0.0,
                        longitude = emergencyDto.longitude ?: 0.0,
                        address = emergencyDto.address,
                        message = emergencyDto.message,
                        createdAt = createdTimestamp,
                        updatedAt = updatedTimestamp,
                        deviceInfo = emergencyDto.deviceInfo,
                        priority = emergencyDto.priority,
                        responseTime = emergencyDto.responseTime
                    )
                }.sortedByDescending { it.createdAt }

                // ✅ DEBUGGING: Mostrar resultado final
                println("📋 RESULTADO FINAL:")
                history.forEachIndexed { index, item ->
                    println("[$index] ID: ${item.id}")
                    println("[$index] Timestamp: ${item.createdAt}")
                    println("[$index] Formatted: ${formatDebugDate(item.createdAt)}")
                    println("---")
                }

                EmergencyResult.Success(history)
            }
            is EmergencyResult.Error -> result
            is EmergencyResult.Loading -> EmergencyResult.Loading
        }
    }

    // ✅ AGREGAR esta función helper para debugging
    private fun formatDebugDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    // ✅ FUNCIÓN: Obtener emergencia por ID
    suspend fun getEmergencyById(
        authToken: String,
        emergencyId: Long
    ): EmergencyResult<EmergencyHistory?> {
        return when (val result = emergencyRepository.getEmergencyById(authToken, emergencyId)) {
            is EmergencyResult.Success -> {
                val emergencyDto = result.data
                if (emergencyDto != null) {
                    val history = EmergencyHistory(
                        id = emergencyDto.id,
                        userId = emergencyDto.userId,
                        emergencyType = emergencyDto.emergencyType ?: "panic_button",
                        status = parseEmergencyStatus(emergencyDto.status),
                        latitude = emergencyDto.latitude ?: 0.0,
                        longitude = emergencyDto.longitude ?: 0.0,
                        address = emergencyDto.address,
                        message = emergencyDto.message,
                        createdAt = parseTimestamp(emergencyDto.createdAt),
                        updatedAt = emergencyDto.updatedAt?.let { parseTimestamp(it) },
                        deviceInfo = emergencyDto.deviceInfo,
                        priority = emergencyDto.priority,
                        responseTime = emergencyDto.responseTime
                    )
                    EmergencyResult.Success(history)
                } else {
                    EmergencyResult.Success(null)
                }
            }
            is EmergencyResult.Error -> result
            is EmergencyResult.Loading -> EmergencyResult.Loading
        }
    }

    // ✅ FUNCIÓN: Obtener emergencias por rango de fechas
    suspend fun getEmergencyHistoryByDateRange(
        authToken: String,
        userId: String,
        startDate: Long,
        endDate: Long
    ): EmergencyResult<List<EmergencyHistory>> {
        return when (val result = getEmergencyHistory(authToken, userId)) {
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

    // ✅ FUNCIÓN: Obtener emergencias por estado
    suspend fun getEmergencyHistoryByStatus(
        authToken: String,
        userId: String,
        status: EmergencyStatus
    ): EmergencyResult<List<EmergencyHistory>> {
        return when (val result = getEmergencyHistory(authToken, userId)) {
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

    // ✅ FUNCIÓN: Obtener estadísticas del historial
    suspend fun getEmergencyStatistics(
        authToken: String,
        userId: String
    ): EmergencyResult<EmergencyStatistics> {
        return when (val result = getEmergencyHistory(authToken, userId)) {
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

    // ✅ FUNCIÓN: Buscar en historial
    suspend fun searchEmergencyHistory(
        authToken: String,
        userId: String,
        query: String
    ): EmergencyResult<List<EmergencyHistory>> {
        return when (val result = getEmergencyHistory(authToken, userId)) {
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

    private fun parseEmergencyStatus(status: String?): EmergencyStatus {
        return when (status?.lowercase()) {
            "active" -> EmergencyStatus.ACTIVE
            "completed" -> EmergencyStatus.COMPLETED
            "cancelled" -> EmergencyStatus.CANCELLED
            "pending" -> EmergencyStatus.PENDING
            "cancelling" -> EmergencyStatus.CANCELLING
            else -> EmergencyStatus.INACTIVE
        }
    }

    private fun parseTimestamp(timestamp: String?): Long {
        return try {
            if (timestamp.isNullOrBlank()) {
                return System.currentTimeMillis()
            }

            println("🔧 Parsing timestamp: '$timestamp'")

            // ✅ EXTRACCIÓN MANUAL DE COMPONENTES
            // Formato: 2025-06-17T04:49:05.348404+00:00

            val cleanTimestamp = timestamp
                .replace("T", " ")           // 2025-06-17 04:49:05.348404+00:00
                .split(".")[0]               // 2025-06-17 04:49:05
                .split("+")[0]               // 2025-06-17 04:49:05
                .trim()

            println("🔄 Cleaned: '$cleanTimestamp'")

            // ✅ PARSEAR CON FORMATO SIMPLE
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("UTC")

            val date = formatter.parse(cleanTimestamp)
            val result = date?.time ?: System.currentTimeMillis()

            println("✅ SUCCESS: $result (${formatDebugDate(result)})")
            return result

        } catch (e: Exception) {
            println("💥 ERROR parsing timestamp: $timestamp - ${e.message}")
            return System.currentTimeMillis()
        }
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