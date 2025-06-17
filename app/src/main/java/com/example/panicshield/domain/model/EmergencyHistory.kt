package com.example.panicshield.domain.model

/**
 * Modelo de dominio para el historial de emergencias
 */
data class EmergencyHistory(
    val id: Long? = null,
    val userId: String,
    val emergencyType: String,
    val status: EmergencyStatus,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val message: String? = null,
    val createdAt: Long,
    val updatedAt: Long? = null,
    val deviceInfo: String? = null,
    val priority: String? = null,
    val responseTime: Int? = null
) {

    // ✅ PROPIEDADES CALCULADAS

    /**
     * Duración de la emergencia en milisegundos
     */
    val duration: Long
        get() = if (updatedAt != null && updatedAt > createdAt) {
            updatedAt - createdAt
        } else {
            0L
        }

    /**
     * Indica si la emergencia está activa
     */
    val isActive: Boolean
        get() = status == EmergencyStatus.ACTIVE || status == EmergencyStatus.PENDING

    /**
     * Indica si la emergencia fue resuelta exitosamente
     */
    val wasResolved: Boolean
        get() = status == EmergencyStatus.COMPLETED

    /**
     * Indica si la emergencia fue cancelada por el usuario
     */
    val wasCancelled: Boolean
        get() = status == EmergencyStatus.CANCELLED

    /**
     * Obtiene el nombre del tipo de emergencia formateado
     */
    val formattedEmergencyType: String
        get() = when (emergencyType) {
            "panic_button" -> "Botón de Pánico"
            "medical" -> "Emergencia Médica"
            "fire" -> "Incendio"
            "police" -> "Emergencia Policial"
            "accident" -> "Accidente"
            else -> emergencyType.replace("_", " ").split(" ")
                .joinToString(" ") { word ->
                    word.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }
                }
        }

    /**
     * Obtiene el color asociado al estado de la emergencia
     */
    val statusColor: String
        get() = when (status) {
            EmergencyStatus.ACTIVE -> "#E53935"      // Rojo
            EmergencyStatus.PENDING -> "#FF9800"     // Naranja
            EmergencyStatus.COMPLETED -> "#4CAF50"   // Verde
            EmergencyStatus.CANCELLED -> "#757575"   // Gris
            EmergencyStatus.CANCELLING -> "#FF9800"  // Naranja
            EmergencyStatus.INACTIVE -> "#2196F3"    // Azul
        }

    /**
     * Obtiene la descripción del estado en español
     */
    val statusDescription: String
        get() = when (status) {
            EmergencyStatus.ACTIVE -> "Emergencia activa"
            EmergencyStatus.PENDING -> "Enviando alerta"
            EmergencyStatus.COMPLETED -> "Emergencia resuelta"
            EmergencyStatus.CANCELLED -> "Cancelada por usuario"
            EmergencyStatus.CANCELLING -> "Cancelando emergencia"
            EmergencyStatus.INACTIVE -> "Inactiva"
        }

    /**
     * Indica si la emergencia tiene información de ubicación válida
     */
    val hasValidLocation: Boolean
        get() = latitude != 0.0 && longitude != 0.0

    /**
     * Obtiene las coordenadas formateadas para mostrar
     */
    val formattedCoordinates: String
        get() = if (hasValidLocation) {
            "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
        } else {
            "Ubicación no disponible"
        }

    companion object {

        /**
         * Crea una instancia de prueba para testing
         */
        fun createSample(
            id: Long = 1L,
            emergencyType: String = "panic_button",
            status: EmergencyStatus = EmergencyStatus.COMPLETED,
            createdAt: Long = System.currentTimeMillis()
        ): EmergencyHistory {
            return EmergencyHistory(
                id = id,
                userId = "sample_user_id",
                emergencyType = emergencyType,
                status = status,
                latitude = -16.4090,  // Arequipa, Perú
                longitude = -71.5375,
                address = "Av. Ejercito 123, Arequipa, Perú",
                message = "Emergencia de prueba",
                createdAt = createdAt,
                updatedAt = createdAt + 300000, // 5 minutos después
                deviceInfo = """{"model":"Android Device","version":"14"}""",
                priority = "high",
                responseTime = 180000 // 3 minutos
            )
        }

        /**
         * Crea múltiples emergencias de prueba
         */
        fun createSampleList(count: Int = 5): List<EmergencyHistory> {
            val types = listOf("panic_button", "medical", "fire", "police", "accident")
            val statuses = listOf(
                EmergencyStatus.COMPLETED,
                EmergencyStatus.CANCELLED,
                EmergencyStatus.ACTIVE,
                EmergencyStatus.PENDING
            )

            return (1..count).map { index ->
                val baseTime = System.currentTimeMillis() - (index * 86400000L) // Días anteriores
                createSample(
                    id = index.toLong(),
                    emergencyType = types[index % types.size],
                    status = statuses[index % statuses.size],
                    createdAt = baseTime
                )
            }
        }
    }
}