package com.example.panicshield.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val phoneNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

sealed class AuthResult {
    object Loading : AuthResult()
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}