package com.example.panicshield.data.remote.dto

import com.google.gson.annotations.SerializedName


data class ContactDto(
    @SerializedName("id")
    val id: Long? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("user_id")
    val userId: String? = null,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone")
    val phone: String
)

data class CreateContactDto(
    @SerializedName("user_id")
    val userId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone")
    val phone: String
)

data class UpdateContactDto(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("phone")
    val phone: String
)