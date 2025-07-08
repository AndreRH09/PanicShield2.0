package com.example.panicshield.di

import android.content.Context
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.local.dao.ContactDao
import com.example.panicshield.data.remote.api.AuthApi
import com.example.panicshield.data.remote.api.EmergencyApi
import com.example.panicshield.data.remote.repository.AuthRepository
import com.example.panicshield.data.remote.repository.ContactRepository
import com.example.panicshield.data.remote.repository.EmergencyRepository
import com.example.panicshield.data.sync.SyncConfiguration
import com.example.supabaseofflinesupport.SyncManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.panicshield.data.remote.config.SupabaseConfig
import io.github.jan.supabase.SupabaseClient


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
        contactDao: ContactDao,
        tokenManager: TokenManager,
        syncManager: SyncManager,
        syncConfiguration: SyncConfiguration,
        @ApplicationContext context: Context
    ): ContactRepository {
        return ContactRepository(contactDao, tokenManager, syncManager, syncConfiguration, context)
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return SupabaseConfig.client
    }

    @Provides
    @Singleton
    fun provideSyncConfiguration(
        syncManager: SyncManager,
        contactDao: ContactDao,
        @ApplicationContext context: Context
    ): SyncConfiguration {
        return SyncConfiguration(syncManager, contactDao, context)
    }

}
