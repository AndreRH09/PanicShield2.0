package com.example.panicshield.domain.usecase

import com.example.panicshield.data.remote.repository.AuthRepository
import com.example.panicshield.domain.model.AuthResponse
import com.example.panicshield.domain.model.AuthResult
import com.example.panicshield.domain.model.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult<AuthResponse> {
        return if (isValidEmail(email) && isValidPassword(password)) {
            authRepository.login(email, password)
        } else {
            AuthResult.Error(
                com.example.panicshield.domain.model.AuthError(
                    code = 400,
                    errorCode = "invalid_input",
                    message = "Email o contraseña inválidos"
                )
            )
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): AuthResult<User> {
        return if (isValidEmail(email) && isValidPassword(password)) {
            authRepository.register(email, password)
        } else {
            AuthResult.Error(
                com.example.panicshield.domain.model.AuthError(
                    code = 400,
                    errorCode = "invalid_input",
                    message = "Email o contraseña inválidos"
                )
            )
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}

class IsLoggedInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return authRepository.isLoggedIn()
    }
}

class GetCurrentUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<String?> {
        return authRepository.getUserId()
    }
}