package com.example.panicshield.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenManager @Inject constructor(
    private val context: Context
) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")
    private val userIdKey = stringPreferencesKey("user_id")
    private val userEmailKey = stringPreferencesKey("user_email")

    suspend fun saveTokens(accessToken: String, refreshToken: String, userId: String, userEmail: String) {
        context.dataStore.edit { preferences ->
            preferences[accessTokenKey] = accessToken
            preferences[refreshTokenKey] = refreshToken
            preferences[userIdKey] = userId
            preferences[userEmailKey] = userEmail
        }
    }

    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    fun getAccessToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[accessTokenKey]
        }
    }

    fun getRefreshToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[refreshTokenKey]
        }
    }

    fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[userIdKey]
        }
    }

    fun getUserEmail(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[userEmailKey]
        }
    }

    fun isLoggedIn(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[accessTokenKey] != null
        }
    }
}