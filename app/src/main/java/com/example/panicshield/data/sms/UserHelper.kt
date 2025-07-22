package com.example.panicshield.data.sms

import android.content.Context
import android.content.SharedPreferences
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth

class UserHelper(
    private val context: Context,
    private val supabaseClient: SupabaseClient
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // Guarda el teléfono del usuario en SharedPreferences cuando se loguea
    fun saveUserPhone(phone: String) {
        prefs.edit().putString("user_phone", phone).apply()
    }

    // Obtiene el teléfono del usuario
    fun getUserPhone(): String {
        return prefs.getString("user_phone", "") ?: ""
    }

    // Obtiene el nombre del usuario
    fun getUserName(): String {
        val currentUser = supabaseClient.auth.currentUserOrNull()
        return currentUser?.userMetadata?.get("name")?.toString() ?: "Usuario"
    }

    // Obtiene el ID del usuario
    fun getUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    // Limpia los datos del usuario al cerrar sesión
    fun clearUserData() {
        prefs.edit().clear().apply()
    }

    // Verifica si el usuario está logueado
    fun isUserLoggedIn(): Boolean {
        return supabaseClient.auth.currentUserOrNull() != null
    }
}