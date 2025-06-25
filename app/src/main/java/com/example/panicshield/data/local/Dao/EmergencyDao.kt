package com.example.panicshield.data.local.dao

import androidx.room.*
import com.example.panicshield.data.local.entity.EmergencyEntity
import com.example.panicshield.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyDao {

    // ===== OPERACIONES BÁSICAS =====

    @Query("SELECT * FROM emergencies WHERE is_deleted = 0 ORDER BY created_at DESC")
    fun getAllEmergencies(): Flow<List<EmergencyEntity>>

    @Query("SELECT * FROM emergencies WHERE is_deleted = 0 ORDER BY created_at DESC")
    suspend fun getAllEmergenciesSync(): List<EmergencyEntity>

    @Query("SELECT * FROM emergencies WHERE user_id = :userId AND is_deleted = 0 ORDER BY created_at DESC")
    fun getUserEmergencies(userId: String): Flow<List<EmergencyEntity>>

    @Query("SELECT * FROM emergencies WHERE user_id = :userId AND is_deleted = 0 ORDER BY created_at DESC")
    suspend fun getUserEmergenciesSync(userId: String): List<EmergencyEntity>

    @Query("SELECT * FROM emergencies WHERE id = :id AND is_deleted = 0")
    suspend fun getEmergencyById(id: Long): EmergencyEntity?

    @Query("SELECT * FROM emergencies WHERE local_id = :localId AND is_deleted = 0")
    suspend fun getEmergencyByLocalId(localId: String): EmergencyEntity?

    @Query("SELECT * FROM emergencies WHERE user_id = :userId AND status IN ('pending', 'active') AND is_deleted = 0 ORDER BY created_at DESC LIMIT 1")
    suspend fun getCurrentActiveEmergency(userId: String): EmergencyEntity?

    @Query("SELECT * FROM emergencies WHERE user_id = :userId AND status IN ('pending', 'active') AND is_deleted = 0 ORDER BY created_at DESC LIMIT 1")
    fun getCurrentActiveEmergencyFlow(userId: String): Flow<EmergencyEntity?>

    @Query("SELECT * FROM emergencies WHERE status = :status AND is_deleted = 0 ORDER BY created_at DESC")
    suspend fun getEmergenciesByStatus(status: String): List<EmergencyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergency(emergency: EmergencyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencies(emergencies: List<EmergencyEntity>)

    @Update
    suspend fun updateEmergency(emergency: EmergencyEntity)

    @Query("UPDATE emergencies SET is_deleted = 1, deleted_at = :deletedAt WHERE id = :id")
    suspend fun softDeleteEmergency(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun hardDeleteEmergency(emergency: EmergencyEntity)

    @Query("DELETE FROM emergencies WHERE id = :id")
    suspend fun hardDeleteEmergencyById(id: Long)

    // ===== OPERACIONES DE SINCRONIZACIÓN =====

    @Query("SELECT * FROM emergencies WHERE sync_status = :syncStatus AND is_deleted = 0")
    suspend fun getEmergenciesBySyncStatus(syncStatus: String): List<EmergencyEntity>

    @Query("SELECT * FROM emergencies WHERE needs_upload = 1 AND is_deleted = 0 ORDER BY created_at ASC")
    suspend fun getEmergenciesNeedingUpload(): List<EmergencyEntity>

    @Query("SELECT * FROM emergencies WHERE needs_download = 1 AND is_deleted = 0")
    suspend fun getEmergenciesNeedingDownload(): List<EmergencyEntity>

    @Query("SELECT * FROM emergencies WHERE sync_status = 'FAILED' AND is_deleted = 0")
    suspend fun getFailedSyncEmergencies(): List<EmergencyEntity>

    @Query("SELECT * FROM emergencies WHERE sync_status = 'CONFLICT' AND is_deleted = 0")
    suspend fun getConflictEmergencies(): List<EmergencyEntity>

    @Query("UPDATE emergencies SET sync_status = :syncStatus, last_sync_at = :lastSyncAt WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, syncStatus: String, lastSyncAt: Long = System.currentTimeMillis())

    @Query("UPDATE emergencies SET sync_status = :syncStatus, last_sync_at = :lastSyncAt WHERE local_id = :localId")
    suspend fun updateSyncStatusByLocalId(localId: String, syncStatus: String, lastSyncAt: Long = System.currentTimeMillis())

    @Query("UPDATE emergencies SET needs_upload = :needsUpload WHERE id = :id")
    suspend fun updateNeedsUpload(id: Long, needsUpload: Boolean)

    @Query("UPDATE emergencies SET needs_upload = :needsUpload WHERE local_id = :localId")
    suspend fun updateNeedsUploadByLocalId(localId: String, needsUpload: Boolean)

    @Query("UPDATE emergencies SET needs_download = :needsDownload WHERE id = :id")
    suspend fun updateNeedsDownload(id: Long, needsDownload: Boolean)

    @Query("UPDATE emergencies SET sync_version = :version WHERE id = :id")
    suspend fun updateSyncVersion(id: Long, version: Long)

    @Query("UPDATE emergencies SET id = :remoteId, sync_status = 'SYNCED', last_sync_at = :lastSyncAt, needs_upload = 0 WHERE local_id = :localId")
    suspend fun updateWithRemoteId(localId: String, remoteId: Long, lastSyncAt: Long = System.currentTimeMillis())

    // ===== OPERACIONES PARA TRABAJO OFFLINE =====

    @Query("SELECT COUNT(*) FROM emergencies WHERE user_id = :userId AND status IN ('pending', 'active') AND is_deleted = 0")
    suspend fun hasActiveEmergency(userId: String): Int

    @Query("SELECT COUNT(*) FROM emergencies WHERE needs_upload = 1 AND is_deleted = 0")
    suspend fun getPendingUploadCount(): Int

    @Query("SELECT COUNT(*) FROM emergencies WHERE sync_status = 'FAILED' AND is_deleted = 0")
    suspend fun getFailedSyncCount(): Int

    @Query("SELECT MAX(updated_at) FROM emergencies WHERE user_id = :userId AND is_deleted = 0")
    suspend fun getLastUpdateTime(userId: String): Long?

    // ===== OPERACIONES DE LIMPIEZA =====

    @Query("DELETE FROM emergencies WHERE is_deleted = 1 AND deleted_at < :cutoffTime")
    suspend fun cleanupDeletedEmergencies(cutoffTime: Long)

    @Query("DELETE FROM emergencies WHERE sync_status = 'SYNCED' AND updated_at < :cutoffTime AND is_deleted = 0")
    suspend fun cleanupOldSyncedEmergencies(cutoffTime: Long)

    @Query("UPDATE emergencies SET sync_status = 'PENDING', needs_upload = 1 WHERE sync_status = 'FAILED'")
    suspend fun retryFailedSync()

    // ===== OPERACIONES BATCH =====

    @Transaction
    suspend fun upsertEmergency(emergency: EmergencyEntity): Long {
        val existingEmergency = if (emergency.id != null) {
            getEmergencyById(emergency.id)
        } else if (emergency.localId != null) {
            getEmergencyByLocalId(emergency.localId)
        } else {
            null
        }

        return if (existingEmergency != null) {
            updateEmergency(emergency.copy(
                id = existingEmergency.id,
                localId = existingEmergency.localId
            ))
            existingEmergency.id ?: 0L
        } else {
            insertEmergency(emergency)
        }
    }

    @Transaction
    suspend fun syncEmergencyFromRemote(remoteEmergency: EmergencyEntity) {
        val localEmergency = getEmergencyById(remoteEmergency.id ?: return)

        if (localEmergency == null) {
            // No existe localmente, insertar
            insertEmergency(remoteEmergency.copy(
                syncStatus = SyncStatus.SYNCED.name,
                lastSyncAt = System.currentTimeMillis(),
                needsUpload = false,
                needsDownload = false
            ))
        } else {
            // Existe localmente, verificar conflictos
            if (localEmergency.syncVersion < remoteEmergency.syncVersion) {
                // El remoto es más nuevo
                if (localEmergency.needsUpload) {
                    // Hay conflicto
                    updateEmergency(remoteEmergency.copy(
                        syncStatus = SyncStatus.CONFLICT.name,
                        lastSyncAt = System.currentTimeMillis()
                    ))
                } else {
                    // Actualizar con datos remotos
                    updateEmergency(remoteEmergency.copy(
                        syncStatus = SyncStatus.SYNCED.name,
                        lastSyncAt = System.currentTimeMillis(),
                        needsUpload = false,
                        needsDownload = false
                    ))
                }
            }
            // Si local es más nuevo o igual, no hacer nada
        }
    }

    @Transaction
    suspend fun markForUpload(id: Long) {
        updateNeedsUpload(id, true)
        updateSyncStatus(id, SyncStatus.PENDING.name)
    }

    @Transaction
    suspend fun markForUploadByLocalId(localId: String) {
        updateNeedsUploadByLocalId(localId, true)
        updateSyncStatusByLocalId(localId, SyncStatus.PENDING.name)
    }

    // ===== OPERACIONES DE CONSULTA ESPECÍFICAS =====

    @Query("SELECT * FROM emergencies WHERE updated_at > :timestamp AND is_deleted = 0 ORDER BY updated_at ASC")
    suspend fun getEmergenciesUpdatedAfter(timestamp: Long): List<EmergencyEntity>

    @Query("SELECT * FROM emergencies WHERE last_sync_at IS NULL OR last_sync_at < :timestamp AND is_deleted = 0")
    suspend fun getEmergenciesNotSyncedSince(timestamp: Long): List<EmergencyEntity>

    @Query("SELECT DISTINCT user_id FROM emergencies WHERE is_deleted = 0")
    suspend fun getAllUserIds(): List<String>
}