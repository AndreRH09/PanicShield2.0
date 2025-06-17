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
    val userId: String? = null,

    @SerializedName("emergency_type")
    val emergencyType: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("priority")
    val priority: String? = null,

    @SerializedName("device_info")
    @JsonAdapter(JsonMapDeserializer::class)
    val deviceInfo: Map<String, Any>? = null,

    @SerializedName("response_time")
    val responseTime: Int? = null,

    @SerializedName("responder_info")
    @JsonAdapter(JsonMapDeserializer::class)
    val responderInfo: Map<String, Any>? = null
)

data class CreateEmergencyDto(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("emergency_type")
    val emergencyType: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("priority")
    val priority: String,

    @SerializedName("device_info")
    val deviceInfo: Map<String, Any>? = null
)

data class UpdateEmergencyDto(
    @SerializedName("emergency_type")
    val emergencyType: String? = null,

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
    val priority: String? = null,

    @SerializedName("device_info")
    val deviceInfo: Map<String, Any>? = null,

    @SerializedName("response_time")
    val responseTime: Int? = null,

    @SerializedName("responder_info")
    val responderInfo: Map<String, Any>? = null
)