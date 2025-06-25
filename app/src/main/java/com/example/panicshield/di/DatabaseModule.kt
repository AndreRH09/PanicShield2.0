// DatabaseModule.kt
package com.example.panicshield.di

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.panicshield.data.local.dao.ContactDao
import com.example.panicshield.data.local.entity.ContactEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(
    entities = [ContactEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PanicShieldDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PanicShieldDatabase {
        return Room.databaseBuilder(
            context,
            PanicShieldDatabase::class.java,
            "panic_shield_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideContactDao(database: PanicShieldDatabase): ContactDao {
        return database.contactDao()
    }
}