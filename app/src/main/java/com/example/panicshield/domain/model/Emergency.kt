package com.example.panicshield.domain.model

data class Emergency(
    val id: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val userId: String,
    val emergencyType: EmergencyType,
    val status: EmergencyStatus,
    val location: EmergencyLocation,
    val message: String? = null,
    val priority: EmergencyPriority = EmergencyPriority.HIGH,
    val deviceInfo: DeviceInfo? = null,
    val responseTime: Int? = null,
    val responderInfo: ResponderInfo? = null
)

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
        fun fromApiValue(value: String): EmergencyType = when (value) {
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
        fun fromApiValue(value: String): EmergencyStatus = when (value) {
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
        fun fromApiValue(value: String): EmergencyPriority = when (value) {
            "LOW" -> LOW
            "MEDIUM" -> MEDIUM
            "HIGH" -> HIGH
            "CRITICAL" -> CRITICAL
            else -> HIGH
        }
    }
}

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