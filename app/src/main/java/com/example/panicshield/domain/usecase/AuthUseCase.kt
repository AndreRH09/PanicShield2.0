package com.example.panicshield.domain.usecase


import com.example.panicshield.data.repository.AuthRepository
import com.example.panicshield.domain.model.AuthResult
import com.example.panicshield.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    fun getAuthState(): Flow<Boolean> = authRepository.getAuthState()

    suspend fun signIn(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank()) {
            return AuthResult.Error("Email y contraseña son requeridos")
        }

        if (!isValidEmail(email)) {
            return AuthResult.Error("Email no válido")
        }

        if (password.length < 6) {
            return AuthResult.Error("La contraseña debe tener al menos 6 caracteres")
        }

        return authRepository.signIn(email, password)
    }

    suspend fun signUp(email: String, password: String, displayName: String, confirmPassword: String): AuthResult {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            return AuthResult.Error("Todos los campos son requeridos")
        }

        if (!isValidEmail(email)) {
            return AuthResult.Error("Email no válido")
        }

        if (password.length < 6) {
            return AuthResult.Error("La contraseña debe tener al menos 6 caracteres")
        }

        if (password != confirmPassword) {
            return AuthResult.Error("Las contraseñas no coinciden")
        }

        return authRepository.signUp(email, password, displayName)
    }

    suspend fun signOut() = authRepository.signOut()

    fun getCurrentUser(): User? = authRepository.getCurrentUser()

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}