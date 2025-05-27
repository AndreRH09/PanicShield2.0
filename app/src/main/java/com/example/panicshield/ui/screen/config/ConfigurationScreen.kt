package com.example.panicshield.ui.screen.config

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ConfigurationScreen(
    onSignOut: () -> Unit
) {
    var locationEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var autoSendEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Sección de Privacidad y Seguridad
        ConfigSection(title = "Privacidad y Seguridad") {
            ConfigItem(
                icon = Icons.Default.LocationOn,
                title = "Compartir Ubicación",
                subtitle = "Envía tu ubicación en alertas de emergencia",
                checked = locationEnabled,
                onCheckedChange = { locationEnabled = it }
            )

            ConfigItem(
                icon = Icons.Default.Security,
                title = "Envío Automático",
                subtitle = "Envía alerta automáticamente después de 10 segundos",
                checked = autoSendEnabled,
                onCheckedChange = { autoSendEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sección de Notificaciones
        ConfigSection(title = "Notificaciones") {
            ConfigItem(
                icon = Icons.Default.Notifications,
                title = "Notificaciones Push",
                subtitle = "Recibe confirmaciones de alertas enviadas",
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it }
            )

            ConfigItem(
                icon = Icons.Default.VolumeUp,
                title = "Sonido de Alerta",
                subtitle = "Reproduce sonido al activar el botón de pánico",
                checked = soundEnabled,
                onCheckedChange = { soundEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de cerrar sesión
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Cuenta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cerrar Sesión")
                }
            }
        }
    }
}

@Composable
private fun ConfigSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun ConfigItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}