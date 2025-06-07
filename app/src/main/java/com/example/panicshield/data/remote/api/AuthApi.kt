package com.example.panicshield.data.remote.api

import com.example.panicshield.data.remote.dto.AuthResponseDto
import com.example.panicshield.data.remote.dto.RegisterResponseDto
import com.example.panicshield.domain.model.LoginRequest
import com.example.panicshield.domain.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST("auth/v1/signup")
    suspend fun register(
        @Header("apikey") apiKey: String,
        @Body request: RegisterRequest
    ): Response<RegisterResponseDto>

    @POST("auth/v1/token")
    suspend fun login(
        @Header("apikey") apiKey: String,
        @Query("grant_type") grantType: String = "password",
        @Body request: LoginRequest
    ): Response<AuthResponseDto>

    //companion object {
    //    const val BASE_URL = "https://qwdoulitlxkzkhkjbicz.supabase.co/"
    //    const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF3ZG91bGl0bHhremtoa2piaWN6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDc4ODEwMTgsImV4cCI6MjA2MzQ1NzAxOH0.zTjry20mHP0GDW6fAhgBwLrN6JrGx8HijOSPNDdGtd8"
    //}
}