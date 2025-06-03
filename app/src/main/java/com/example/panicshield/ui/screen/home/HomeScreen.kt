package com.example.panicshield.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    // Observar estados del ViewModel usando collectAsState
    val uiState by viewModel.uiState.collectAsState()
    val locationInfo by viewModel.locationInfo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Componente 1: Estado de conexión
        ConnectionStatusCard(connectionStatus = uiState.connectionStatus)

        Spacer(modifier = Modifier.height(16.dp))

        // Título principal
        Text(
            text = "Botón de Pánico",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Estado actual del sistema
        SystemStatusCard(
            isPanicActivated = uiState.isPanicActivated,
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Botón de pánico principal
        PanicButton(
            isPanicActivated = uiState.isPanicActivated,
            isLoading = uiState.isLoading,
            onPanicClick = { viewModel.togglePanicButton() }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Información adicional
        if (!uiState.isPanicActivated) {
            Text(
                text = "Mantén presionado por 3 segundos para activar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Componente 2: Estadísticas básicas usando datos del ViewModel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                icon = Icons.Default.LocationOn,
                label = "Ubicación",
                value = viewModel.getLocationStatus(),
                isActive = locationInfo?.isLocationActive ?: false
            )

            StatCard(
                icon = Icons.Default.Shield,
                label = "Estado",
                value = viewModel.getCurrentStatus(),
                isActive = !uiState.isPanicActivated
            )
        }

        // Mostrar información de ubicación si está disponible
        locationInfo?.let { location ->
            Spacer(modifier = Modifier.height(16.dp))

            LocationInfoCard(location = location)
        }
    }
}

@Composable
private fun ConnectionStatusCard(connectionStatus: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de estado
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.Green)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = connectionStatus,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SystemStatusCard(
    isPanicActivated: Boolean,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPanicActivated)
                Color.Red.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when {
                    isLoading -> "PROCESANDO..."
                    isPanicActivated -> "¡ALARMA ACTIVADA!"
                    else -> "Sistema Listo"
                },
                style = MaterialTheme.typography.titleLarge,
                color = if (isPanicActivated) Color.Red else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isPanicActivated)
                    "Notificaciones enviadas a contactos de emergencia"
                else
                    "Presiona el botón en caso de emergencia",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PanicButton(
    isPanicActivated: Boolean,
    isLoading: Boolean,
    onPanicClick: () -> Unit
) {
    Button(
        onClick = onPanicClick,
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isLoading -> Color.Yellow
                isPanicActivated -> Color.Gray
                else -> Color.Red
            },
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 8.dp,
            pressedElevation = 16.dp
        ),
        enabled = !isLoading
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Botón de Pánico",
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    isLoading -> "ENVIANDO..."
                    isPanicActivated -> "DETENER"
                    else -> "PÁNICO"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    isActive: Boolean = true
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.secondaryContainer
            else
                Color.Red.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = if (isActive)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    Color.Red
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isActive)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    Color.Red
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    Color.Red
            )
        }
    }
}

@Composable
private fun LocationInfoCard(location: LocationInfo) {
    Card(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                "Ubicación Actual", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = location.address,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Lat: ${location.latitude}, Lng: ${location.longitude}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}