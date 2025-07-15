package com.example.panicshield.data.local.dao

import androidx.room.*
import com.example.panicshield.data.local.entity.EmergencyHistoryCacheEntity

@Dao
interface EmergencyHistoryDao {

    @Query("SELECT * FROM emergency_history_cache WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getHistoryByUser(userId: String): List<EmergencyHistoryCacheEntity>

    @Query("SELECT * FROM emergency_history_cache WHERE needsSync = 1")
    suspend fun getPendingSyncItems(): List<EmergencyHistoryCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<EmergencyHistoryCacheEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: EmergencyHistoryCacheEntity)

    @Query("UPDATE emergency_history_cache SET needsSync = 0, lastSyncedAt = :timestamp WHERE id = :id")
    suspend fun markAsSynced(id: Long, timestamp: Long)

    @Query("DELETE FROM emergency_history_cache")
    suspend fun clearAll()

    @Query("SELECT MAX(lastSyncedAt) FROM emergency_history_cache")
    suspend fun getLastSyncTimestamp(): Long?
}