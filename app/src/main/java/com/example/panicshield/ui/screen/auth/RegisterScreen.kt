package com.example.panicshield.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Botón de regreso
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color(0xFFE53E3E)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Logo
            Card(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(50.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE53E3E))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_call),
                        contentDescription = "Logo",
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Crear Cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE53E3E)
            )

            Text(
                text = "Regístrate para protegerte",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de email
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = viewModel::updateEmail,
                label = { Text("Correo electrónico") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = "Email")
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = uiState.errorMessage != null && uiState.errorMessage!!.contains("email", ignoreCase = true)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de contraseña
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = viewModel::updatePassword,
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Password")
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility
                                         else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ocultar contraseña"
                                               else "Mostrar contraseña"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                     else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = uiState.errorMessage != null && uiState.errorMessage!!.contains("contraseña", ignoreCase = true)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de confirmar contraseña
            OutlinedTextField(
                value = viewModel.confirmPassword,
                onValueChange = viewModel::updateConfirmPassword,
                label = { Text("Confirmar contraseña") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Confirm Password")
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility
                                         else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) "Ocultar contraseña"
                                               else "Mostrar contraseña"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                                     else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = uiState.errorMessage != null && uiState.errorMessage!!.contains("coinciden", ignoreCase = true)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mensaje de ayuda para contraseña
            Text(
                text = "La contraseña debe tener al menos 6 caracteres",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de registro
            Button(
                onClick = viewModel::register,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading &&
                         viewModel.email.isNotBlank() &&
                         viewModel.password.isNotBlank() &&
                         viewModel.confirmPassword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Crear Cuenta",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mensaje de error o éxito
            uiState.errorMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("exitoso"))
                            Color(0xFFE8F5E8) else Color(0xFFFFEBEE)
                    )
                ) {
                    Text(
                        text = message,
                        color = if (message.contains("exitoso"))
                            Color(0xFF2E7D32) else Color(0xFFE53E3E),
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Link para login
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "¿Ya tienes cuenta? Inicia sesión",
                    color = Color(0xFFE53E3E)
                )
            }
        }
    }
}