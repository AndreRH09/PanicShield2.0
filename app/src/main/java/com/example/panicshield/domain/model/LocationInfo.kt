package com.example.panicshield.domain.model

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double? = null,
    val address: String? = null,
    val timestamp: Long,
    val isLocationActive: Boolean
)