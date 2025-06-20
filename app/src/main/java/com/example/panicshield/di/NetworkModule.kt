package com.example.panicshield.di

import android.content.Context
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.remote.api.EmergencyApi
import com.example.panicshield.data.remote.api.AuthApi
import com.example.panicshield.data.remote.api.ApiConstants
import com.example.panicshield.data.remote.api.ContactApi

// Imports actualizados según la nueva estructura
import com.example.panicshield.data.remote.repository.AuthRepository
import com.example.panicshield.data.remote.repository.EmergencyRepository
import com.example.panicshield.domain.usecase.EmergencyUseCase
import com.example.panicshield.domain.usecase.LocationUseCase

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // ===== APIS =====

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEmergencyApi(retrofit: Retrofit): EmergencyApi {
        return retrofit.create(EmergencyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideContactApi(retrofit: Retrofit): ContactApi {
        return retrofit.create(ContactApi::class.java)
    }

    // ===== UTILITIES =====

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    // ===== REPOSITORIES =====

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApi,
        tokenManager: TokenManager,
        gson: Gson
    ): AuthRepository {
        return AuthRepository(authApi, tokenManager, gson)
    }

    @Provides
    @Singleton
    fun provideEmergencyRepository(
        emergencyApi: EmergencyApi,
        tokenManager: TokenManager
    ): EmergencyRepository {
        return EmergencyRepository(emergencyApi, tokenManager)
    }

    // ===== USE CASES =====

    @Provides
    @Singleton
    fun provideEmergencyUseCase(
        emergencyRepository: EmergencyRepository,
        tokenManager: TokenManager
    ): EmergencyUseCase {
        return EmergencyUseCase(emergencyRepository)
    }

    @Provides
    @Singleton
    fun provideLocationUseCase(
        @ApplicationContext context: Context
    ): LocationUseCase = LocationUseCase(context)
}