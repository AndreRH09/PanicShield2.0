package com.example.panicshield.domain.model

import com.google.gson.Gson

data class PanicAlert(
    val userId: String,
    val userName: String,
    val userPhone: String,
    val emergencyType: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val message: String,
    val priority: String,
    val timestamp: Long,
    val deviceInfo: Map<String, Any>? = null
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): PanicAlert = Gson().fromJson(json, PanicAlert::class.java)
    }
}