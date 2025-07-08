// data/remote/config/SupabaseConfig.kt
package com.example.panicshield.data.remote.config

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import com.example.panicshield.data.remote.api.ApiConstants




object SupabaseConfig {

    // Reemplaza con tu URL y clave de Supabase
    private const val SUPABASE_URL = ApiConstants.BASE_URL
    private const val SUPABASE_ANON_KEY = ApiConstants.API_KEY
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest) {
            serializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        install(Auth)
    }
}