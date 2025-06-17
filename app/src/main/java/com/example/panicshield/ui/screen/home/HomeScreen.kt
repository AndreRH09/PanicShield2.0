package com.example.panicshield.ui.screen.home

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.panicshield.domain.model.EmergencyStatus
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locationInfo by viewModel.locationInfo.collectAsStateWithLifecycle()

    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    var requestedPermission by rememberSaveable { mutableStateOf(false) }

    // ✅ Observar cambios de conectividad en tiempo real
    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted && !requestedPermission) {
            permissionState.launchPermissionRequest()
            requestedPermission = true
        }
        // viewModel.startConnectivityMonitoring()
    }
    LaunchedEffect(permissionState.status.isGranted) {
        if (permissionState.status.isGranted) {
            viewModel.refreshLocation()  // Llama a tu función que actualiza la ubicación
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Botón de Emergencia",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Menu */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                }
            )
        },

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ✅ INDICADOR DE CONEXIÓN CON SUSPEND
            ConnectionStatusIndicator(
                connectionStatus = uiState.connectionStatus,
                onRetry = { viewModel.retryConnection() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ✅ BOTÓN DE EMERGENCIA
            EmergencyButton(
                isActivated = uiState.isPanicActivated,
                isLoading = uiState.isLoading,
                emergencyStatus = uiState.emergencyStatus,
                onClick = { viewModel.togglePanicButton() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ TEXTO DE ESTADO
            StatusText(emergencyStatus = uiState.emergencyStatus)

            Spacer(modifier = Modifier.weight(1f))

            // ✅ CARD DE INFORMACIÓN DE UBICACIÓN
            LocationInfoCard(
                locationInfo = locationInfo,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )
        }
    }

    // ✅ MANEJO DE ERRORES
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // TODO: Mostrar SnackBar o Dialog con error
        }
    }
}

@Composable
fun ConnectionStatusIndicator(
    connectionStatus: ConnectionStatus,
    onRetry: () -> Unit
) {
    val (statusText, statusColor, statusIcon) = when (connectionStatus) {
        ConnectionStatus.CONNECTED -> Triple(
            "Conectado",
            Color(0xFF4CAF50), // Verde
            Icons.Default.CheckCircle
        )
        ConnectionStatus.CONNECTING -> Triple(
            "Conectando...",
            Color(0xFFFFC107), // Amarillo
            Icons.Default.Sync
        )
        ConnectionStatus.DISCONNECTED -> Triple(
            "Desconectado",
            Color(0xFFFF9800), // Naranja
            Icons.Default.WifiOff
        )
        ConnectionStatus.ERROR -> Triple(
            "Error de conexión",
            Color(0xFFE91E63), // Rojo
            Icons.Default.ErrorOutline
        )
    }

    Row(
        modifier = Modifier
            .background(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = statusColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable {
                if (connectionStatus == ConnectionStatus.ERROR ||
                    connectionStatus == ConnectionStatus.DISCONNECTED) {
                    onRetry()
                }
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = statusIcon,
            contentDescription = null,
            tint = statusColor,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = statusText,
            color = statusColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        // ✅ Indicador de loading para conectando
        if (connectionStatus == ConnectionStatus.CONNECTING) {
            Spacer(modifier = Modifier.width(8.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 2.dp,
                color = statusColor
            )
        }
    }
}

@Composable
fun EmergencyButton(
    isActivated: Boolean,
    isLoading: Boolean,
    emergencyStatus: EmergencyStatus,
    onClick: () -> Unit
) {
    val buttonColor = when {
        isLoading -> Color(0xFFFF9800) // Naranja para loading
        isActivated -> Color(0xFFE53935) // Rojo para activado
        else -> Color(0xFFE53935) // Rojo por defecto
    }

    val iconColor = Color.White
    val buttonSize = 200.dp

    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(CircleShape)
            .background(buttonColor)
            .border(
                width = 4.dp,
                color = buttonColor.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 4.dp,
                modifier = Modifier.size(60.dp)
            )
        } else {
            Icon(
                imageVector = if (isActivated) Icons.Default.Shield else Icons.Default.Security,
                contentDescription = "Emergency Button",
                tint = iconColor,
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

@Composable
fun StatusText(emergencyStatus: EmergencyStatus) {
    val statusText = when (emergencyStatus) {
        EmergencyStatus.ACTIVE -> "ALERTA ACTIVA"
        EmergencyStatus.PENDING -> "ENVIANDO..."
        EmergencyStatus.CANCELLING -> "CANCELANDO..."
        EmergencyStatus.CANCELLED -> "CANCELADA"
        EmergencyStatus.COMPLETED -> "COMPLETADA"
        EmergencyStatus.INACTIVE -> "EMERGENCIA"
    }

    val subText = when (emergencyStatus) {
        EmergencyStatus.ACTIVE -> "Ayuda en camino"
        EmergencyStatus.PENDING -> "Enviando alerta..."
        EmergencyStatus.CANCELLING -> "Cancelando alerta..."
        EmergencyStatus.CANCELLED -> "Alerta cancelada"
        EmergencyStatus.COMPLETED -> "Emergencia resuelta"
        EmergencyStatus.INACTIVE -> "Presione en caso de peligro"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = statusText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subText,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LocationInfoCard(
    locationInfo: com.example.panicshield.domain.model.LocationInfo?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ✅ HEADER DEL CARD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Ubicación",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Información de Ubicación",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                // ✅ INDICADOR DE ESTADO GPS
                LocationStatusIndicator(isActive = locationInfo?.isLocationActive ?: false)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ INFORMACIÓN DE COORDENADAS
            if (locationInfo != null) {
                LocationInfoRow(
                    icon = Icons.Default.MyLocation,
                    label = "Latitud:",
                    value = String.format("%.6f", locationInfo.latitude)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LocationInfoRow(
                    icon = Icons.Default.GpsFixed,
                    label = "Longitud:",
                    value = String.format("%.6f", locationInfo.longitude)
                )

                Spacer(modifier = Modifier.height(8.dp))



                Spacer(modifier = Modifier.height(8.dp))

                LocationInfoRow(
                    icon = Icons.Default.Schedule,
                    label = "Actualizado:",
                    value = formatTimestamp(locationInfo.timestamp)
                )
            } else {
                // ✅ ESTADO SIN UBICACIÓN
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationDisabled,
                        contentDescription = "Sin ubicación",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ubicación no disponible",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Verifica los permisos de ubicación",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun LocationStatusIndicator(isActive: Boolean) {
    val (color, icon) = if (isActive) {
        Color(0xFF4CAF50) to Icons.Default.GpsFixed
    } else {
        Color(0xFFE91E63) to Icons.Default.GpsOff
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isActive) "GPS Activo" else "GPS Inactivo",
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (isActive) "Activo" else "Inactivo",
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun LocationInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF757575),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF757575),
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}



// ✅ FUNCIÓN HELPER PARA FORMATEAR TIMESTAMP
private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}