package com.example.panicshield.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "emergency_history_cache",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["createdAt"])
    ]
)
data class EmergencyHistoryCacheEntity(
    @PrimaryKey val id: Long,
    val userId: String,
    val emergencyType: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val message: String?,
    val createdAt: Long,
    val updatedAt: Long?,
    val deviceInfo: String?,
    val priority: String?,
    val responseTime: Int?,

    // Campos para sincronización
    val lastSyncedAt: Long = 0L,
    val needsSync: Boolean = false
)