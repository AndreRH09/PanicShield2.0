package com.example.panicshield.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.supabaseofflinesupport.BaseSyncableEntity

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    override val id: Int,
    override var lastUpdatedTimestamp: Long,
    override val offlineFieldOpType: Int,
    val userId: String,
    val name: String,
    val phone: String,
    val createdAt: String? = null
) : BaseSyncableEntity()