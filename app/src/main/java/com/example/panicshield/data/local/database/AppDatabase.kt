package com.example.panicshield.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.panicshield.data.local.dao.ContactDao
import com.example.panicshield.data.local.dao.EmergencyHistoryDao
import com.example.panicshield.data.local.entity.ContactEntity
import com.example.panicshield.data.local.entity.EmergencyHistoryCacheEntity

@Database(
    entities = [ContactEntity::class,
        EmergencyHistoryCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun emergencyHistoryDao(): EmergencyHistoryDao



    companion object {
        const val DATABASE_NAME = "panicshield_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null


    }
}