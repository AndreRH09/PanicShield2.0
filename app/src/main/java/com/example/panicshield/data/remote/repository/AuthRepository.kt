package com.example.panicshield.data.remote.repository

import com.google.gson.Gson
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.remote.api.AuthApi
import com.example.panicshield.data.remote.api.ApiConstants
import com.example.panicshield.data.remote.dto.AuthErrorDto
import com.example.panicshield.data.remote.dto.toDomain
import com.example.panicshield.domain.model.AuthError
import com.example.panicshield.domain.model.AuthResponse
import com.example.panicshield.domain.model.AuthResult
import com.example.panicshield.domain.model.LoginRequest
import com.example.panicshield.domain.model.RegisterRequest
import com.example.panicshield.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val gson: Gson
) {

    suspend fun register(email: String, password: String): AuthResult<User> {
        return try {
            val response = authApi.register(
                apiKey = ApiConstants.API_KEY,
                request = RegisterRequest(email, password)
            )

            if (response.isSuccessful) {
                response.body()?.let { registerResponse ->
                    AuthResult.Success(registerResponse.toDomain())
                } ?: AuthResult.Error(AuthError(0, "unknown_error", "Unknown error occurred"))
            } else {
                val errorBody = response.errorBody()?.string()
                val authError = try {
                    gson.fromJson(errorBody, AuthErrorDto::class.java)
                } catch (e: Exception) {
                    AuthErrorDto(response.code(), "unknown_error", "Unknown error occurred")
                }

                AuthResult.Error(
                    AuthError(
                        code = authError.code,
                        errorCode = authError.errorCode,
                        message = authError.msg
                    )
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(
                AuthError(
                    code = 0,
                    errorCode = "network_error",
                    message = e.message ?: "Network error occurred"
                )
            )
        }
    }

    suspend fun login(email: String, password: String): AuthResult<AuthResponse> {
        return try {
            val response = authApi.login(
                apiKey = ApiConstants.API_KEY,
                request = LoginRequest(email, password)
            )

            if (response.isSuccessful) {
                response.body()?.let { authResponse ->
                    val domainResponse = authResponse.toDomain()

                    // Guardar tokens en DataStore
                    tokenManager.saveTokens(
                        accessToken = domainResponse.accessToken,
                        refreshToken = domainResponse.refreshToken,
                        userId = domainResponse.user.id,
                        userEmail = domainResponse.user.email
                    )

                    AuthResult.Success(domainResponse)
                } ?: AuthResult.Error(AuthError(0, "unknown_error", "Unknown error occurred"))
            } else {
                val errorBody = response.errorBody()?.string()
                val authError = try {
                    gson.fromJson(errorBody, AuthErrorDto::class.java)
                } catch (e: Exception) {
                    AuthErrorDto(response.code(), "unknown_error", "Unknown error occurred")
                }

                AuthResult.Error(
                    AuthError(
                        code = authError.code,
                        errorCode = authError.errorCode,
                        message = authError.msg
                    )
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(
                AuthError(
                    code = 0,
                    errorCode = "network_error",
                    message = e.message ?: "Network error occurred"
                )
            )
        }
    }

    suspend fun logout() {
        tokenManager.clearTokens()
    }

    fun isLoggedIn(): Flow<Boolean> {
        return tokenManager.isLoggedIn()
    }

    fun getUserId(): Flow<String?> {
        return tokenManager.getUserId()
    }

    fun getUserEmail(): Flow<String?> {
        return tokenManager.getUserEmail()
    }
}