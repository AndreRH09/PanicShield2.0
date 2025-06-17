package com.example.panicshield.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EmergencyDto(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

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
    val priority: String = "HIGH",

    @SerializedName("device_info")
    val deviceInfo: String? = null,

    @SerializedName("response_time")
    val responseTime: Int? = null,

    @SerializedName("responder_info")
    val responderInfo: String? = null
)

data class CreateEmergencyDto(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("emergency_type")
    val emergencyType: String,

    @SerializedName("status")
    val status: String = "pending",

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("priority")
    val priority: String = "HIGH",

    @SerializedName("device_info")
    val deviceInfo: String? = null
)

data class UpdateEmergencyDto(
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

    @SerializedName("response_time")
    val responseTime: Int? = null,

    @SerializedName("responder_info")
    val responderInfo: String? = null
)