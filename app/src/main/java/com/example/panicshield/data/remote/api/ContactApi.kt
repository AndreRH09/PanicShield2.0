package com.example.panicshield.data.remote.api

import com.example.panicshield.data.remote.dto.ContactDto
import com.example.panicshield.data.remote.dto.CreateContactDto
import com.example.panicshield.data.remote.dto.UpdateContactDto
import retrofit2.Response
import retrofit2.http.*

interface ContactApi {

    @GET("rest/v1/contacts")
    suspend fun getContacts(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("select") select: String = "*",
        @Query("user_id") userId: String
    ): Response<List<ContactDto>>

    @POST("rest/v1/contacts")
    suspend fun createContact(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Prefer") prefer: String = "return=representation",
        @Body contact: CreateContactDto
    ): Response<List<ContactDto>>

    @PATCH("rest/v1/contacts")
    suspend fun updateContact(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Header("Prefer") prefer: String = "return=representation",
        @Query("id") id: String,
        @Body contact: UpdateContactDto
    ): Response<List<ContactDto>>

    @DELETE("rest/v1/contacts")
    suspend fun deleteContact(
        @Header("apikey") apikey: String = ApiConstants.API_KEY,
        @Header("Authorization") authorization: String,
        @Query("id") id: String
    ): Response<Unit>
}