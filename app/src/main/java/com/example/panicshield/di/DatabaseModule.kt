// di/DatabaseModule.kt
package com.example.panicshield.di

import android.content.Context
import androidx.room.Room
import com.example.panicshield.data.local.database.AppDatabase
import com.example.panicshield.data.local.dao.ContactDao
import com.example.panicshield.data.local.dao.EmergencyHistoryDao
import com.example.supabaseofflinesupport.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideContactDao(database: AppDatabase): ContactDao {
        return database.contactDao()
    }

    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        supabaseClient: SupabaseClient
    ): SyncManager {
        return SyncManager(context, supabaseClient)
    }


    @Provides
    fun provideEmergencyHistoryDao(database: AppDatabase): EmergencyHistoryDao {
        return database.emergencyHistoryDao()
    }


}