package com.example.panicshield.di

// Imports actualizados según la nueva estructura

import android.content.Context
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.local.dao.ContactDao
import com.example.panicshield.data.remote.api.AuthApi
import com.example.panicshield.data.remote.api.ContactApi
import com.example.panicshield.data.remote.api.EmergencyApi
import com.example.panicshield.data.remote.repository.AuthRepository
import com.example.panicshield.data.remote.repository.ContactRepository
import com.example.panicshield.data.remote.repository.EmergencyRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        tokenManager: TokenManager,
        gson: Gson
    ): AuthRepository = AuthRepository(authApi, tokenManager, gson)

    @Provides
    @Singleton
    fun provideEmergencyRepository(
        emergencyApi: EmergencyApi,
        tokenManager: TokenManager
    ): EmergencyRepository = EmergencyRepository(emergencyApi, tokenManager)

    @Provides
    @Singleton
    fun provideContactRepository(
        contactApi: ContactApi,
        contactDao: ContactDao,
        tokenManager: TokenManager,
        @ApplicationContext context: Context
    ): ContactRepository = ContactRepository(contactApi, contactDao, tokenManager, context)
}
