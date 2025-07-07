package com.example.panicshield.di

import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.remote.api.AuthApi
import com.example.panicshield.data.remote.api.EmergencyApi
import com.example.panicshield.data.remote.repository.AuthRepository
import com.example.panicshield.data.remote.repository.EmergencyRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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

}
