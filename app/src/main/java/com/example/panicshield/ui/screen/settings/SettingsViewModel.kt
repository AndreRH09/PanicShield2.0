package com.example.panicshield.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panicshield.data.local.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    // Tema oscuro
    val darkModeEnabled: StateFlow<Boolean> = settingsDataStore.darkModeEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDarkMode(enabled)
        }
    }

    // Configuración de SMS
    val sendSmsEnabled: StateFlow<Boolean> = settingsDataStore.sendSmsEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val locationSharingEnabled: StateFlow<Boolean> = settingsDataStore.sendLocation
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val sendPhoneNumberEnabled: StateFlow<Boolean> = settingsDataStore.sendPhoneNumber
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val sendTimestampEnabled: StateFlow<Boolean> = settingsDataStore.sendTime
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val sendPriorityEnabled: StateFlow<Boolean> = settingsDataStore.sendPriority
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun toggleSmsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setSendSmsEnabled(enabled)
        }
    }

    fun toggleLocationSharing(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setSendLocation(enabled)
        }
    }

    fun toggleSendPhoneNumber(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setSendPhoneNumber(enabled)
        }
    }

    fun toggleSendTimestamp(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setSendTime(enabled)
        }
    }

    fun toggleSendPriority(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setSendPriority(enabled)
        }
    }

    // Configuración de alerta
    val moderateAlertTaps: StateFlow<Float> = settingsDataStore.moderateAlertTaps
        .map { it.toFloat() }
        .stateIn(viewModelScope, SharingStarted.Lazily, 2f)

    val severeAlertTaps: StateFlow<Float> = settingsDataStore.severeAlertTaps
        .map { it.toFloat() }
        .stateIn(viewModelScope, SharingStarted.Lazily, 3f)

    val alarmSoundEnabled: StateFlow<Boolean> = settingsDataStore.alarmSoundEnabled
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val confirmBeforeAlarm: StateFlow<Boolean> = settingsDataStore.confirmBeforeAlarm
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun setModerateAlarmTapCount(value: Float) {
        viewModelScope.launch {
            settingsDataStore.setModerateAlertTaps(value.toInt())
        }
    }

    fun setSevereAlarmTapCount(value: Float) {
        viewModelScope.launch {
            settingsDataStore.setSevereAlertTaps(value.toInt())
        }
    }

    fun toggleAlarmSound(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setAlarmSoundEnabled(enabled)
        }
    }

    fun toggleConfirmationMessage(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setConfirmBeforeAlarm(enabled)
        }
    }

    // Acciones adicionales (por si se extienden funcionalidades luego)
    fun editProfile() {}
    fun configureNotifications() {}
    fun configureAlarm() {}
}
