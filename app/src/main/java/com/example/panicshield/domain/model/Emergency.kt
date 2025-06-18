package com.example.panicshield.domain.model

data class Emergency(
    val id: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val userId: String,
    val emergencyType: String, // Mantenemos String para flexibilidad con API
    val status: String, // Mantenemos String para flexibilidad con API
    val latitude: Double? = null, // Campos separados para compatibilidad con MapCreate
    val longitude: Double? = null,
    val address: String? = null,
    val message: String? = null,
    val priority: String? = null, // String para flexibilidad
    val deviceInfo: Map<String, Any>? = null, // Map para flexibilidad
    val responseTime: Int? = null,
    val responderInfo: Map<String, Any>? = null // Map para flexibilidad
) {
    // Propiedades computadas para acceso tipado (de Master)
    val emergencyTypeEnum: EmergencyType
        get() = EmergencyType.fromApiValue(emergencyType)
    
    val statusEnum: EmergencyStatus
        get() = EmergencyStatus.fromApiValue(status)
    
    val priorityEnum: EmergencyPriority
        get() = EmergencyPriority.fromApiValue(priority ?: "HIGH")
    
    // Location como propiedad computada
    val location: EmergencyLocation?
        get() = if (latitude != null && longitude != null) {
            EmergencyLocation(latitude, longitude, address)
        } else null
    
    // Device info tipado
    val deviceInfoTyped: DeviceInfo?
        get() = deviceInfo?.let { info ->
            try {
                DeviceInfo(
                    model = info["model"] as? String ?: "Unknown",
                    androidVersion = info["androidVersion"] as? String ?: "Unknown",
                    appVersion = info["appVersion"] as? String ?: "Unknown",
                    timestamp = (info["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                null
            }
        }
    
    // Responder info tipado
    val responderInfoTyped: ResponderInfo?
        get() = responderInfo?.let { info ->
            try {
                ResponderInfo(
                    responderId = info["responderId"] as? String ?: "",
                    responderName = info["responderName"] as? String ?: "",
                    estimatedArrival = info["estimatedArrival"] as? String,
                    contactNumber = info["contactNumber"] as? String
                )
            } catch (e: Exception) {
                null
            }
        }
}

// Enums del Master para tipado fuerte
enum class EmergencyType {
    PANIC_BUTTON,
    MEDICAL,
    FIRE,
    POLICE;

    fun toApiValue(): String = when (this) {
        PANIC_BUTTON -> "panic_button"
        MEDICAL -> "medical"
        FIRE -> "fire"
        POLICE -> "police"
    }

    companion object {
        fun fromApiValue(value: String): EmergencyType = when (value.lowercase()) {
            "panic_button" -> PANIC_BUTTON
            "medical" -> MEDICAL
            "fire" -> FIRE
            "police" -> POLICE
            else -> PANIC_BUTTON
        }
    }
}

enum class EmergencyStatus {
    INACTIVE,
    PENDING,
    ACTIVE,
    CANCELLING,
    CANCELLED,
    COMPLETED;

    fun toApiValue(): String = when (this) {
        INACTIVE -> "inactive"
        PENDING -> "pending"
        ACTIVE -> "active"
        CANCELLING -> "cancelling"
        CANCELLED -> "cancelled"
        COMPLETED -> "completed"
    }

    companion object {
        fun fromApiValue(value: String): EmergencyStatus = when (value.lowercase()) {
            "pending" -> PENDING
            "active" -> ACTIVE
            "cancelling" -> CANCELLING
            "cancelled" -> CANCELLED
            "completed" -> COMPLETED
            else -> INACTIVE
        }
    }
}

enum class EmergencyPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    fun toApiValue(): String = when (this) {
        LOW -> "LOW"
        MEDIUM -> "MEDIUM"
        HIGH -> "HIGH"
        CRITICAL -> "CRITICAL"
    }

    companion object {
        fun fromApiValue(value: String): EmergencyPriority = when (value.uppercase()) {
            "LOW" -> LOW
            "MEDIUM" -> MEDIUM
            "HIGH" -> HIGH
            "CRITICAL" -> CRITICAL
            else -> HIGH
        }
    }
}

// Data classes del Master para estructuras tipadas
data class EmergencyLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val accuracy: Float? = null
)

data class DeviceInfo(
    val model: String,
    val androidVersion: String,
    val appVersion: String,
    val timestamp: Long
)

data class ResponderInfo(
    val responderId: String,
    val responderName: String,
    val estimatedArrival: String?,
    val contactNumber: String?
)