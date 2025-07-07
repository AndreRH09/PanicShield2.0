package com.example.panicshield.di

// Imports actualizados según la nueva estructura

import android.content.Context
import androidx.room.Room
import com.example.panicshield.data.local.dao.ContactDao
import com.example.panicshield.data.local.database.AppDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "panicshield_db"
        ).build()
    }

    @Provides
    fun provideContactDao(db: AppDatabase): ContactDao = db.contactDao()
}
