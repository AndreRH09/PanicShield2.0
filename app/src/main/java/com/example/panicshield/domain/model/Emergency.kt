package com.example.panicshield.domain.model

data class Emergency(
    val id: Long? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val userId: String, // Mantener no-nullable si es requerido
    val emergencyType: String,
    val status: String,
    //  CORRECCIÓN: Hacer nullable para consistencia con DTO
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val message: String? = null,
    val priority: String? = null, //  Nullable
    val deviceInfo: Map<String, Any>? = null,
    val responseTime: Int? = null,
    val responderInfo: Map<String, Any>? = null
)