package com.example.panicshield.data.remote.dto

import com.example.supabaseofflinesupport.BaseRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContactRemoteDto(
    @SerialName("id")
    override val id: Int,

    @SerialName("timestamp")
    override val lastUpdatedTimestamp: Long,

    @SerialName("user_id")
    val userId: String,

    @SerialName("name")
    val name: String,

    @SerialName("phone")
    val phone: String,

    @SerialName("created_at")
    val createdAt: String? = null
) : BaseRemoteEntity()