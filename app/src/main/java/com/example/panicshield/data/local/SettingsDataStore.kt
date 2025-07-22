package com.example.panicshield.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings_preferences")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val EMERGENCY_ALERTS_ENABLED_KEY = booleanPreferencesKey("emergency_alerts_enabled")
        private val REMINDER_ALERTS_ENABLED_KEY = booleanPreferencesKey("reminder_alerts_enabled")

        // SMS data keys
        private val SEND_SMS_ENABLED_KEY = booleanPreferencesKey("send_sms_enabled")
        private val SMS_INCLUDE_LOCATION_KEY = booleanPreferencesKey("sms_include_location")
        private val SMS_INCLUDE_PHONE_KEY = booleanPreferencesKey("sms_include_phone")
        private val SMS_INCLUDE_TIME_KEY = booleanPreferencesKey("sms_include_time")
        private val SMS_INCLUDE_PRIORITY_KEY = booleanPreferencesKey("sms_include_priority")

        // Alert settings keys
        private val MODERATE_ALERT_TAPS_KEY = intPreferencesKey("moderate_alert_taps")
        private val SEVERE_ALERT_TAPS_KEY = intPreferencesKey("severe_alert_taps")
        private val ALARM_SOUND_ENABLED_KEY = booleanPreferencesKey("alarm_sound_enabled")
        private val CONFIRM_BEFORE_ALARM_KEY = booleanPreferencesKey("confirm_before_alarm")
    }

    // Tema oscuro
    val darkModeEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[DARK_MODE_KEY] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[NOTIFICATIONS_ENABLED_KEY] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[NOTIFICATIONS_ENABLED_KEY] = enabled }
    }

    val emergencyAlertsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[EMERGENCY_ALERTS_ENABLED_KEY] ?: true }

    suspend fun setEmergencyAlertsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[EMERGENCY_ALERTS_ENABLED_KEY] = enabled }
    }

    val reminderAlertsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[REMINDER_ALERTS_ENABLED_KEY] ?: true }

    suspend fun setReminderAlertsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[REMINDER_ALERTS_ENABLED_KEY] = enabled }
    }

    // ========== SMS SETTINGS ==========

    val sendSmsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[SEND_SMS_ENABLED_KEY] ?: false }

    suspend fun setSendSmsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[SEND_SMS_ENABLED_KEY] = enabled }
    }

    val sendLocation: Flow<Boolean> = context.settingsDataStore.data
        .map { it[SMS_INCLUDE_LOCATION_KEY] ?: true }

    suspend fun setSendLocation(enabled: Boolean) {
        context.settingsDataStore.edit { it[SMS_INCLUDE_LOCATION_KEY] = enabled }
    }

    val sendPhoneNumber: Flow<Boolean> = context.settingsDataStore.data
        .map { it[SMS_INCLUDE_PHONE_KEY] ?: true }

    suspend fun setSendPhoneNumber(enabled: Boolean) {
        context.settingsDataStore.edit { it[SMS_INCLUDE_PHONE_KEY] = enabled }
    }

    val sendTime: Flow<Boolean> = context.settingsDataStore.data
        .map { it[SMS_INCLUDE_TIME_KEY] ?: true }

    suspend fun setSendTime(enabled: Boolean) {
        context.settingsDataStore.edit { it[SMS_INCLUDE_TIME_KEY] = enabled }
    }

    val sendPriority: Flow<Boolean> = context.settingsDataStore.data
        .map { it[SMS_INCLUDE_PRIORITY_KEY] ?: true }

    suspend fun setSendPriority(enabled: Boolean) {
        context.settingsDataStore.edit { it[SMS_INCLUDE_PRIORITY_KEY] = enabled }
    }

    // ========== ALERT SETTINGS ==========

    val moderateAlertTaps: Flow<Int> = context.settingsDataStore.data
        .map { it[MODERATE_ALERT_TAPS_KEY] ?: 2 }

    suspend fun setModerateAlertTaps(value: Int) {
        context.settingsDataStore.edit { it[MODERATE_ALERT_TAPS_KEY] = value }
    }

    val severeAlertTaps: Flow<Int> = context.settingsDataStore.data
        .map { it[SEVERE_ALERT_TAPS_KEY] ?: 3 }

    suspend fun setSevereAlertTaps(value: Int) {
        context.settingsDataStore.edit { it[SEVERE_ALERT_TAPS_KEY] = value }
    }

    val alarmSoundEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[ALARM_SOUND_ENABLED_KEY] ?: false }

    suspend fun setAlarmSoundEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[ALARM_SOUND_ENABLED_KEY] = enabled }
    }

    val confirmBeforeAlarm: Flow<Boolean> = context.settingsDataStore.data
        .map { it[CONFIRM_BEFORE_ALARM_KEY] ?: false }

    suspend fun setConfirmBeforeAlarm(enabled: Boolean) {
        context.settingsDataStore.edit { it[CONFIRM_BEFORE_ALARM_KEY] = enabled }
    }
}
