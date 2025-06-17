package com.example.panicshield.data.remote.api

import com.example.panicshield.data.remote.dto.EmergencyDto
import com.example.panicshield.data.remote.dto.CreateEmergencyDto
import com.example.panicshield.data.remote.dto.UpdateEmergencyDto
import retrofit2.Response
import retrofit2.http.*

interface EmergencyApi {

    //  PROBLEMA: Usar @Query("user_id") userId con "eq.$userId" no funciona
    //  SOLUCIÓN: Usar @Query("user_id") userId: String? = null directamente
    @GET("rest/v1/emergencies")
    suspend fun getEmergencies(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("user_id") userId: String? = null, //  Sin "eq." aquí
        @Query("status") status: String? = null,
        @Query("emergency_type") emergencyType: String? = null,
        @Query("priority") priority: String? = null
    ): Response<List<EmergencyDto>>

    @GET("rest/v1/emergencies")
    suspend fun getAllEmergencies(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*"
    ): Response<List<EmergencyDto>>

    @POST("rest/v1/emergencies")
    suspend fun createEmergency(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Prefer") prefer: String = "return=representation",
        @Body emergency: CreateEmergencyDto
    ): Response<List<EmergencyDto>>

    @PATCH("rest/v1/emergencies")
    suspend fun updateEmergency(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Prefer") prefer: String = "return=representation",
        @Query("id") id: String, //  Sin "eq." aquí
        @Body emergency: UpdateEmergencyDto
    ): Response<List<EmergencyDto>>

    @DELETE("rest/v1/emergencies")
    suspend fun deleteEmergency(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("id") id: String //  Sin "eq." aquí
    ): Response<Unit>
}