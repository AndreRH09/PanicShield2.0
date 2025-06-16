package com.example.panicshield.domain.mapper

import com.example.panicshield.data.remote.dto.EmergencyDto
import com.example.panicshield.data.remote.dto.CreateEmergencyDto
import com.example.panicshield.data.remote.dto.UpdateEmergencyDto
import com.example.panicshield.domain.model.*
import com.google.gson.Gson

// Extensión para convertir DTO a Domain Model
fun EmergencyDto.toDomainModel(): Emergency {
    return Emergency(
        id = this.id,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        userId = this.userId,
        emergencyType = EmergencyType.fromApiValue(this.emergencyType),
        status = EmergencyStatus.fromApiValue(this.status),
        location = EmergencyLocation(
            latitude = this.latitude,
            longitude = this.longitude,
            address = this.address
        ),
        message = this.message,
        priority = EmergencyPriority.fromApiValue(this.priority),
        deviceInfo = this.deviceInfo?.let { parseDeviceInfo(it) },
        responseTime = this.responseTime,
        responderInfo = this.responderInfo?.let { parseResponderInfo(it) }
    )
}

// Extensión para convertir Domain Model a CreateDTO
fun Emergency.toCreateDto(): CreateEmergencyDto {
    return CreateEmergencyDto(
        userId = this.userId,
        emergencyType = this.emergencyType.toApiValue(),
        status = this.status.toApiValue(),
        latitude = this.location.latitude,
        longitude = this.location.longitude,
        address = this.location.address,
        message = this.message,
        priority = this.priority.toApiValue(),
        deviceInfo = this.deviceInfo?.let { createDeviceInfoJson(it) }
    )
}

// Extensión para convertir Domain Model a UpdateDTO
fun Emergency.toUpdateDto(): UpdateEmergencyDto {
    return UpdateEmergencyDto(
        status = this.status.toApiValue(),
        latitude = this.location.latitude,
        longitude = this.location.longitude,
        address = this.location.address,
        message = this.message,
        responseTime = this.responseTime,
        responderInfo = this.responderInfo?.let { createResponderInfoJson(it) }
    )
}

// Funciones auxiliares para parsing JSON
private fun parseDeviceInfo(json: String): DeviceInfo? {
    return try {
        val gson = Gson()
        val map = gson.fromJson(json, Map::class.java) as Map<String, Any>
        DeviceInfo(
            model = map["model"] as? String ?: "",
            androidVersion = map["android_version"] as? String ?: "",
            appVersion = map["app_version"] as? String ?: "",
            timestamp = (map["timestamp"] as? Double)?.toLong() ?: 0L
        )
    } catch (e: Exception) {
        null
    }
}

private fun parseResponderInfo(json: String): ResponderInfo? {
    return try {
        val gson = Gson()
        val map = gson.fromJson(json, Map::class.java) as Map<String, Any>
        ResponderInfo(
            responderId = map["responder_id"] as? String ?: "",
            responderName = map["responder_name"] as? String ?: "",
            estimatedArrival = map["estimated_arrival"] as? String,
            contactNumber = map["contact_number"] as? String
        )
    } catch (e: Exception) {
        null
    }
}

private fun createDeviceInfoJson(deviceInfo: DeviceInfo): String {
    return """
        {
            "model": "${deviceInfo.model}",
            "android_version": "${deviceInfo.androidVersion}",
            "app_version": "${deviceInfo.appVersion}",
            "timestamp": ${deviceInfo.timestamp}
        }
    """.trimIndent()
}

private fun createResponderInfoJson(responderInfo: ResponderInfo): String {
    return """
        {
            "responder_id": "${responderInfo.responderId}",
            "responder_name": "${responderInfo.responderName}",
            "estimated_arrival": "${responderInfo.estimatedArrival ?: ""}",
            "contact_number": "${responderInfo.contactNumber ?: ""}"
        }
    """.trimIndent()
}