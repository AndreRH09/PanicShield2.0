package com.example.panicshield.di

import android.content.Context
import com.example.panicshield.data.local.AppDatabase
import com.example.panicshield.data.local.dao.EmergencyHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideEmergencyHistoryDao(database: AppDatabase): EmergencyHistoryDao {
        return database.emergencyHistoryDao()
    }
}