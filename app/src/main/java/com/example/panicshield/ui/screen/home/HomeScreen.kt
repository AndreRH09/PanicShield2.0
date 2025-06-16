package com.example.panicshield.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.panicshield.data.local.TokenManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()

) {
    val uiState by viewModel.uiState.collectAsState()
    val locationInfo by viewModel.locationInfo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Estado de la aplicación
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Estado: ${viewModel.getCurrentStatus()}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Ubicación: ${viewModel.getLocationStatus()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Conexión: ${viewModel.getConnectionStatusText()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de pánico (aquí usarías tu componente personalizado)
        Button(
            onClick = { viewModel.togglePanicButton() },
            enabled = !uiState.isLoading,
            modifier = Modifier.size(200.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = if (uiState.isPanicActivated) "CANCELAR" else "EMERGENCIA"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar errores
        uiState.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Button(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}