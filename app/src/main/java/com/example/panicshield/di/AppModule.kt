package com.example.panicshield.di

import android.content.Context
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.sms.SmsHelper
import com.example.panicshield.data.sms.UserHelper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideSmsHelper(@ApplicationContext context: Context): SmsHelper {
        return SmsHelper(context)
    }

    @Provides
    @Singleton
    fun provideUserHelper(
        @ApplicationContext context: Context,
        supabaseClient: SupabaseClient
    ): UserHelper {
        return UserHelper(context, supabaseClient)
    }

}