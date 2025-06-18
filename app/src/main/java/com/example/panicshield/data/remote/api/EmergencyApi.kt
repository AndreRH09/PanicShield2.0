package com.example.panicshield.data.remote.api

import com.example.panicshield.data.remote.dto.EmergencyDto
import com.example.panicshield.data.remote.dto.CreateEmergencyDto
import com.example.panicshield.data.remote.dto.UpdateEmergencyDto
import retrofit2.Response
import retrofit2.http.*

interface EmergencyApi {

    // Función general para obtener emergencias con filtros opcionales (de MapCreate)
    @GET("rest/v1/emergencies")
    suspend fun getEmergencies(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("user_id") userId: String? = null,
        @Query("status") status: String? = null,
        @Query("emergency_type") emergencyType: String? = null,
        @Query("priority") priority: String? = null
    ): Response<List<EmergencyDto>>

    // Función para obtener todas las emergencias (de MapCreate)
    @GET("rest/v1/emergencies")
    suspend fun getAllEmergencies(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*"
    ): Response<List<EmergencyDto>>

    // Función específica para obtener emergencias de un usuario (de Master)
    @GET("rest/v1/emergencies")
    suspend fun getUserEmergencies(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("user_id") userId: String
    ): Response<List<EmergencyDto>>

    // Función para obtener la emergencia activa actual (de Master)
    @GET("rest/v1/emergencies")
    suspend fun getCurrentActiveEmergency(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("user_id") userId: String,
        @Query("status") status: String = "eq.active",
        @Query("order") order: String = "created_at.desc",
        @Query("limit") limit: Int = 1
    ): Response<List<EmergencyDto>>

    // Función para obtener una emergencia por ID (de Master)
    @GET("rest/v1/emergencies")
    suspend fun getEmergencyById(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("id") id: String
    ): Response<List<EmergencyDto>>

    // Función para obtener historial de emergencias (de Master)
    @GET("rest/v1/emergencies")
    suspend fun getEmergencyHistory(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("user_id") userId: String,
        @Query("order") order: String = "created_at.desc"
    ): Response<List<EmergencyDto>>

    // Función para verificar si hay emergencias activas (de Master)
    @GET("rest/v1/emergencies")
    suspend fun hasActiveEmergency(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "id",
        @Query("user_id") userId: String,
        @Query("status") status: String = "in.(pending,active)"
    ): Response<List<Map<String, Any>>>

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
        @Query("id") id: String,
        @Body emergency: UpdateEmergencyDto
    ): Response<List<EmergencyDto>>

    // Función específica para cancelar emergencias (de Master)
    @PATCH("rest/v1/emergencies")
    suspend fun cancelEmergency(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Prefer") prefer: String = "return=representation",
        @Query("id") id: String,
        @Body cancelData: UpdateEmergencyDto = UpdateEmergencyDto(status = "cancelled")
    ): Response<List<EmergencyDto>>

    @DELETE("rest/v1/emergencies")
    suspend fun deleteEmergency(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("id") id: String
    ): Response<Unit>
}