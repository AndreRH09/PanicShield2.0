package com.example.panicshield.ui.screen.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicshield.domain.model.AuthResult
import com.example.panicshield.domain.usecase.IsLoggedInUseCase
import com.example.panicshield.domain.usecase.LoginUseCase
import com.example.panicshield.domain.usecase.LogoutUseCase
import com.example.panicshield.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    init {
        checkLoginStatus()
    }

    fun updateEmail(newEmail: String) {
        email = newEmail
        clearError()
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
        clearError()
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
        clearError()
    }

    fun login() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = loginUseCase(email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorMessage = null
                    )
                    clearFields()
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = getErrorMessage(result.error.errorCode, result.error.message)
                    )
                }
                is AuthResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = result.isLoading)
                }
            }
        }
    }

    fun register() {
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(errorMessage = "Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = registerUseCase(email, password)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                    clearFields()
                    // Mostrar mensaje de éxito - el usuario necesita verificar su email
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Registro exitoso. Por favor verifica tu email."
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = getErrorMessage(result.error.errorCode, result.error.message)
                    )
                }
                is AuthResult.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = result.isLoading)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = _uiState.value.copy(isLoggedIn = false)
            clearFields()
        }
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            isLoggedInUseCase().collect { isLoggedIn ->
                _uiState.value = _uiState.value.copy(isLoggedIn = isLoggedIn)
            }
        }
    }

    private fun clearError() {
        if (_uiState.value.errorMessage != null) {
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }

    private fun clearFields() {
        email = ""
        password = ""
        confirmPassword = ""
    }

    private fun getErrorMessage(errorCode: String, originalMessage: String): String {
        return when (errorCode) {
            "email_address_invalid" -> "El email ingresado no es válido"
            "weak_password" -> "La contraseña debe tener al menos 6 caracteres"
            "invalid_credentials" -> "Email o contraseña incorrectos"
            "network_error" -> "Error de conexión. Verifica tu internet"
            "invalid_input" -> "Email o contraseña inválidos"
            else -> originalMessage
        }
    }
}