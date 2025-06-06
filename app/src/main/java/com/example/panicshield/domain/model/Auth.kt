package com.example.panicshield.domain.model

data class User(
    val id: String,
    val email: String,
    val emailVerified: Boolean,
    val phoneVerified: Boolean,
    val createdAt: String,
    val updatedAt: String
)

data class AuthResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int,
    val expiresAt: Long,
    val refreshToken: String,
    val user: User
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String
)

data class AuthError(
    val code: Int,
    val errorCode: String,
    val message: String
)

sealed class AuthResult<T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error<T>(val error: AuthError) : AuthResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : AuthResult<T>()
}