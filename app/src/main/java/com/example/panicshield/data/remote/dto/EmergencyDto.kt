package com.example.panicshield.data.remote.dto

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

data class EmergencyDto(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("user_id")
    val userId: String? = null, // Nullable para flexibilidad en consultas generales

    @SerializedName("emergency_type")
    val emergencyType: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("latitude")
    val latitude: Double? = null, // Nullable para casos donde no se tenga ubicación

    @SerializedName("longitude")
    val longitude: Double? = null, // Nullable para casos donde no se tenga ubicación

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("priority")
    val priority: String? = null, // Nullable para flexibilidad

    @SerializedName("device_info")
    @JsonAdapter(JsonMapDeserializer::class)
    val deviceInfo: Map<String, Any>? = null, // Usar Map para mejor manejo de datos

    @SerializedName("response_time")
    val responseTime: Int? = null,

    @SerializedName("responder_info")
    @JsonAdapter(JsonMapDeserializer::class)
    val responderInfo: Map<String, Any>? = null // Usar Map para mejor manejo de datos
)

data class CreateEmergencyDto(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("emergency_type")
    val emergencyType: String,

    @SerializedName("status")
    val status: String = "pending", // Valor por defecto más apropiado

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("priority")
    val priority: String = "HIGH", // Valor por defecto para emergencias

    @SerializedName("device_info")
    val deviceInfo: Map<String, Any>? = null // Usar Map para consistencia
)

data class UpdateEmergencyDto(
    @SerializedName("emergency_type")
    val emergencyType: String? = null, // Incluir para actualizaciones completas

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("priority")
    val priority: String? = null, // Incluir para actualizaciones de prioridad

    @SerializedName("device_info")
    val deviceInfo: Map<String, Any>? = null, // Usar Map para consistencia

    @SerializedName("response_time")
    val responseTime: Int? = null,

    @SerializedName("responder_info")
    val responderInfo: Map<String, Any>? = null // Usar Map para consistencia
)