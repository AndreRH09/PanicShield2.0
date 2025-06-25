package com.example.panicshield.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.panicshield.data.local.dao.EmergencyDao
import com.example.panicshield.data.local.entity.EmergencyEntity

@Database(
    entities = [EmergencyEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun emergencyDao(): EmergencyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "panic_shield_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration() // Solo para desarrollo
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migración de versión 1 a 2 (agregar campos de sincronización)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar columnas de sincronización
                database.execSQL(
                    """
                    ALTER TABLE emergencies ADD COLUMN sync_status TEXT NOT NULL DEFAULT 'PENDING'
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE emergencies ADD COLUMN local_id TEXT
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE emergencies ADD COLUMN last_sync_at INTEGER
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE emergencies ADD COLUMN sync_version INTEGER NOT NULL DEFAULT 1
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE emergencies ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE emergencies ADD COLUMN deleted_at INTEGER
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE emergencies ADD COLUMN conflict_resolution TEXT
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE emergencies ADD COLUMN needs_upload INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    ALTER TABLE emergencies ADD COLUMN needs_download INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )

                // Crear índices para mejorar performance de consultas de sincronización
                database.execSQL(
                    """
                    CREATE INDEX index_emergencies_sync_status ON emergencies(sync_status)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX index_emergencies_needs_upload ON emergencies(needs_upload)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX index_emergencies_last_sync_at ON emergencies(last_sync_at)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX index_emergencies_local_id ON emergencies(local_id)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX index_emergencies_user_status ON emergencies(user_id, status, is_deleted)
                    """.trimIndent()
                )
            }
        }

        fun clearAllTables(context: Context) {
            getDatabase(context).clearAllTables()
        }
    }
}

// Converters para tipos complejos
class Converters {
    // Aquí puedes agregar conversores para tipos complejos si los necesitas
    // Por ejemplo, para convertir Map<String, Any> a JSON y viceversa

    /*
    @TypeConverter
    fun fromMapToJson(map: Map<String, Any>?): String? {
        return map?.let { Gson().toJson(it) }
    }

    @TypeConverter
    fun fromJsonToMap(json: String?): Map<String, Any>? {
        return json?.let {
            Gson().fromJson(it, object : TypeToken<Map<String, Any>>() {}.type)
        }
    }
    */
}