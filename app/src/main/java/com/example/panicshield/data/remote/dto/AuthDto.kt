package com.example.panicshield.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.example.panicshield.domain.model.User
import com.example.panicshield.domain.model.AuthResponse

data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("user_metadata")
    val userMetadata: UserMetadataDto,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class UserMetadataDto(
    @SerializedName("email_verified")
    val emailVerified: Boolean,
    @SerializedName("phone_verified")
    val phoneVerified: Boolean
)

data class AuthResponseDto(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expiresIn: Int,
    @SerializedName("expires_at")
    val expiresAt: Long,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("user")
    val user: UserDto
)

data class RegisterResponseDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("user_metadata")
    val userMetadata: UserMetadataDto,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class AuthErrorDto(
    @SerializedName("code")
    val code: Int,
    @SerializedName("error_code")
    val errorCode: String,
    @SerializedName("msg")
    val msg: String
)

// Extension functions para mapear DTOs a modelos de dominio
fun UserDto.toDomain(): User {
    return User(
        id = id,
        email = email,
        emailVerified = userMetadata.emailVerified,
        phoneVerified = userMetadata.phoneVerified,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AuthResponseDto.toDomain(): AuthResponse {
    return AuthResponse(
        accessToken = accessToken,
        tokenType = tokenType,
        expiresIn = expiresIn,
        expiresAt = expiresAt,
        refreshToken = refreshToken,
        user = user.toDomain()
    )
}

fun RegisterResponseDto.toDomain(): User {
    return User(
        id = id,
        email = email,
        emailVerified = userMetadata.emailVerified,
        phoneVerified = userMetadata.phoneVerified,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}