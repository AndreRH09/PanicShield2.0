package com.example.panicshield.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: Long? = null,
    val createdAt: String? = null,
    val userId: String,
    val name: String,
    val phone: String,
    val lastFetchedTime: Long = System.currentTimeMillis()
)
