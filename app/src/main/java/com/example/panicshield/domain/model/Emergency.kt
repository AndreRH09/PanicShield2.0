package com.example.panicshield.domain.model

data class Emergency(
    val id: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val userId: String,
    val emergencyType: String,
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val message: String? = null,
    val priority: String? = null,
    val deviceInfo: Map<String, Any>? = null,
    val responseTime: Int? = null,
    val responderInfo: Map<String, Any>? = null
)