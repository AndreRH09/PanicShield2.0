package com.example.panicshield.ui.screen.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicshield.domain.model.AuthResult
import com.example.panicshield.domain.model.AuthState
import com.example.panicshield.domain.usecase.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var displayName by mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            authUseCase.getAuthState().collect { isAuthenticated ->
                _authState.value = _authState.value.copy(
                    isAuthenticated = isAuthenticated,
                    user = if (isAuthenticated) authUseCase.getCurrentUser() else null
                )
            }
        }
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

    fun updateDisplayName(newDisplayName: String) {
        displayName = newDisplayName
        clearError()
    }

    fun signIn() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = authUseCase.signIn(email, password)) {
                is AuthResult.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = result.user,
                        error = null
                    )
                    clearFields()
                }
                is AuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is AuthResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)

            when (val result = authUseCase.signUp(email, password, displayName, confirmPassword)) {
                is AuthResult.Success -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = result.user,
                        error = null
                    )
                    clearFields()
                }
                is AuthResult.Error -> {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is AuthResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authUseCase.signOut()
            clearFields()
        }
    }

    private fun clearError() {
        if (_authState.value.error != null) {
            _authState.value = _authState.value.copy(error = null)
        }
    }

    private fun clearFields() {
        email = ""
        password = ""
        confirmPassword = ""
        displayName = ""
    }
}
