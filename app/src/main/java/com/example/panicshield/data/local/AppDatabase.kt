package com.example.panicshield.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.panicshield.data.local.dao.EmergencyHistoryDao
import com.example.panicshield.data.local.entity.EmergencyHistoryCacheEntity

@Database(
    entities = [EmergencyHistoryCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun emergencyHistoryDao(): EmergencyHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "panic_shield_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}