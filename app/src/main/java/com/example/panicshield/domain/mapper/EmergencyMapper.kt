package com.example.panicshield.domain.mapper

import com.example.panicshield.data.remote.dto.EmergencyDto
import com.example.panicshield.data.remote.dto.CreateEmergencyDto
import com.example.panicshield.data.remote.dto.UpdateEmergencyDto
import com.example.panicshield.domain.model.*
import com.google.gson.Gson

// Extensión para convertir DTO a Domain Model (compatible con el modelo actual)
fun EmergencyDto.toDomainModel(): Emergency {
    return Emergency(
        id = this.id,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        userId = this.userId ?: "",
        emergencyType = this.emergencyType,
        status = this.status,
        latitude = this.latitude,
        longitude = this.longitude,
        address = this.address,
        message = this.message,
        priority = this.priority,
        deviceInfo = this.deviceInfo,
        responseTime = this.responseTime,
        responderInfo = this.responderInfo
    )
}

// Extensión para convertir Domain Model a CreateDTO
fun Emergency.toCreateDto(): CreateEmergencyDto {
    return CreateEmergencyDto(
        userId = this.userId,
        emergencyType = this.emergencyType,
        status = this.status,
        latitude = this.latitude ?: 0.0,
        longitude = this.longitude ?: 0.0,
        address = this.address,
        message = this.message,
        priority = this.priority ?: "HIGH",
        deviceInfo = this.deviceInfo
    )
}

// Extensión para convertir Domain Model a UpdateDTO
fun Emergency.toUpdateDto(): UpdateEmergencyDto {
    return UpdateEmergencyDto(
        emergencyType = this.emergencyType,
        status = this.status,
        latitude = this.latitude,
        longitude = this.longitude,
        address = this.address,
        message = this.message,
        priority = this.priority,
        deviceInfo = this.deviceInfo,
        responseTime = this.responseTime,
        responderInfo = this.responderInfo
    )
}

// Función helper para crear CreateEmergencyDto desde parámetros tipados
fun createEmergencyDto(
    userId: String,
    emergencyType: EmergencyType,
    status: EmergencyStatus = EmergencyStatus.PENDING,
    location: EmergencyLocation,
    message: String? = null,
    priority: EmergencyPriority = EmergencyPriority.HIGH,
    deviceInfo: DeviceInfo? = null
): CreateEmergencyDto {
    return CreateEmergencyDto(
        userId = userId,
        emergencyType = emergencyType.toApiValue(),
        status = status.toApiValue(),
        latitude = location.latitude,
        longitude = location.longitude,
        address = location.address,
        message = message,
        priority = priority.toApiValue(),
        deviceInfo = deviceInfo?.let { createDeviceInfoMap(it) }
    )
}

// Función helper para crear UpdateEmergencyDto desde parámetros tipados
fun createUpdateEmergencyDto(
    emergencyType: EmergencyType? = null,
    status: EmergencyStatus? = null,
    location: EmergencyLocation? = null,
    message: String? = null,
    priority: EmergencyPriority? = null,
    deviceInfo: DeviceInfo? = null,
    responseTime: Int? = null,
    responderInfo: ResponderInfo? = null
): UpdateEmergencyDto {
    return UpdateEmergencyDto(
        emergencyType = emergencyType?.toApiValue(),
        status = status?.toApiValue(),
        latitude = location?.latitude,
        longitude = location?.longitude,
        address = location?.address,
        message = message,
        priority = priority?.toApiValue(),
        deviceInfo = deviceInfo?.let { createDeviceInfoMap(it) },
        responseTime = responseTime,
        responderInfo = responderInfo?.let { createResponderInfoMap(it) }
    )
}

// ===== FUNCIONES HELPER PRIVADAS =====

private fun createDeviceInfoMap(deviceInfo: DeviceInfo): Map<String, Any> {
    return mapOf(
        "model" to deviceInfo.model,
        "androidVersion" to deviceInfo.androidVersion,
        "appVersion" to deviceInfo.appVersion,
        "timestamp" to deviceInfo.timestamp
    )
}

private fun createResponderInfoMap(responderInfo: ResponderInfo): Map<String, Any> {
    return mapOf(
        "responderId" to responderInfo.responderId,
        "responderName" to responderInfo.responderName,
        "estimatedArrival" to (responderInfo.estimatedArrival ?: ""),
        "contactNumber" to (responderInfo.contactNumber ?: "")
    )
}

// Funciones auxiliares para parsing (si necesitas convertir de Map a objetos tipados)
fun parseDeviceInfo(deviceInfo: Map<String, Any>?): DeviceInfo? {
    return deviceInfo?.let { info ->
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
}

fun parseResponderInfo(responderInfo: Map<String, Any>?): ResponderInfo? {
    return responderInfo?.let { info ->
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