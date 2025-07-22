package com.example.panicshield.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun SettingsScreen(
    navController: NavController,
    onLogout: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val darkModeEnabled by settingsViewModel.darkModeEnabled.collectAsState()
    val isSmsEnabled by settingsViewModel.sendSmsEnabled.collectAsState()
    val locationSharingEnabled by settingsViewModel.locationSharingEnabled.collectAsState()
    val sendPhoneNumberEnabled by settingsViewModel.sendPhoneNumberEnabled.collectAsState()
    val sendTimestampEnabled by settingsViewModel.sendTimestampEnabled.collectAsState()
    val sendPriorityEnabled by settingsViewModel.sendPriorityEnabled.collectAsState()

    val enableConfirmationMessage by settingsViewModel.confirmBeforeAlarm.collectAsState()
    val playAlarmSound by settingsViewModel.alarmSoundEnabled.collectAsState()
    val moderateAlarmTapCount by settingsViewModel.moderateAlertTaps.collectAsState()
    val severeAlarmTapCount by settingsViewModel.severeAlertTaps.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Configuración General
        SettingsSection(title = "Configuración General") {
            SettingSwitch(
                title = "Modo Oscuro",
                checked = darkModeEnabled,
                onCheckedChange = settingsViewModel::toggleDarkMode
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Configuración de Alerta
        SettingsSection(title = "Configuración de Alerta") {
            SettingSwitch(
                title = "Confirmar antes de activar alerta",
                checked = enableConfirmationMessage,
                onCheckedChange = settingsViewModel::toggleConfirmationMessage
            )

            SettingSwitch(
                title = "Reproducir sonido de alerta",
                checked = playAlarmSound,
                onCheckedChange = settingsViewModel::toggleAlarmSound
            )

            SettingSlider(
                title = "Toques para alerta moderada: ${moderateAlarmTapCount.toInt()}",
                value = moderateAlarmTapCount,
                onValueChange = settingsViewModel::setModerateAlarmTapCount,
                valueRange = 1f..5f,
                steps = 3
            )

            SettingSlider(
                title = "Toques para alerta severa: ${severeAlarmTapCount.toInt()}",
                value = severeAlarmTapCount,
                onValueChange = settingsViewModel::setSevereAlarmTapCount,
                valueRange = 1f..10f,
                steps = 8
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Configuración de SMS
        SettingsSection(title = "Configuración de SMS (opcional)") {
            val smsButtonText = if (isSmsEnabled) "Desactivar envío de SMS" else "Enviar SMS a contactos"

            FilledTonalButton(
                onClick = { settingsViewModel.toggleSmsEnabled(!isSmsEnabled) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(smsButtonText)
            }

            if (isSmsEnabled) {
                Spacer(modifier = Modifier.height(12.dp))

                SettingSwitch(
                    title = "Incluir ubicación",
                    checked = locationSharingEnabled,
                    onCheckedChange = settingsViewModel::toggleLocationSharing
                )
                SettingSwitch(
                    title = "Incluir número de teléfono",
                    checked = sendPhoneNumberEnabled,
                    onCheckedChange = settingsViewModel::toggleSendPhoneNumber
                )
                SettingSwitch(
                    title = "Incluir hora",
                    checked = sendTimestampEnabled,
                    onCheckedChange = settingsViewModel::toggleSendTimestamp
                )
                SettingSwitch(
                    title = "Incluir prioridad",
                    checked = sendPriorityEnabled,
                    onCheckedChange = settingsViewModel::toggleSendPriority
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Cerrar sesión
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cerrar Sesión", color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}


@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
fun SettingSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(title)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled
        )
    }
}
