package com.example.panicshield.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.example.panicshield.domain.model.*

@Entity(tableName = "emergencies")
data class EmergencyEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long? = null,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "emergency_type")
    val emergencyType: String,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "address")
    val address: String? = null,

    @ColumnInfo(name = "message")
    val message: String? = null,

    @ColumnInfo(name = "priority")
    val priority: String,

    @ColumnInfo(name = "device_info")
    val deviceInfo: String? = null, // JSON string

    @ColumnInfo(name = "response_time")
    val responseTime: Int? = null,

    @ColumnInfo(name = "responder_info")
    val responderInfo: String? = null, // JSON string

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    // Campos para sincronización
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING.name, // PENDING, SYNCED, FAILED

    @ColumnInfo(name = "local_id")
    val localId: String? = null, // UUID local para emergencias sin ID remoto

    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Long? = null,

    @ColumnInfo(name = "sync_version")
    val syncVersion: Long = 1L, // Para control de versiones

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false, // Soft delete

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    @ColumnInfo(name = "conflict_resolution")
    val conflictResolution: String? = null, // REMOTE_WINS, LOCAL_WINS, MANUAL

    @ColumnInfo(name = "needs_upload")
    val needsUpload: Boolean = false, // Indica si necesita subirse al servidor

    @ColumnInfo(name = "needs_download")
    val needsDownload: Boolean = false // Indica si necesita descargarse del servidor
)

// Enum para estados de sincronización
enum class SyncStatus {
    PENDING,    // Pendiente de sincronizar
    SYNCED,     // Sincronizado correctamente
    FAILED,     // Falló la sincronización
    CONFLICT,   // Hay conflicto de datos
    UPLOADING,  // Subiendo al servidor
    DOWNLOADING // Descargando del servidor
}

// Extensiones para conversión
fun EmergencyEntity.toDomainModel(): Emergency {
    return Emergency(
        id = this.id,
        userId = this.userId,
        emergencyTypeString = this.emergencyType,
        statusString = this.status,
        latitude = this.latitude,
        longitude = this.longitude,
        address = this.address,
        message = this.message,
        priorityString = this.priority,
        deviceInfoMap = this.deviceInfo?.let {
            // Convertir JSON string a Map
            try {
                // Aquí deberías usar tu parser JSON preferido (Gson, Moshi, etc.)
                emptyMap<String, Any>()
            } catch (e: Exception) {
                emptyMap()
            }
        },
        responseTime = this.responseTime,
        responderInfoMap = this.responderInfo?.let {
            try {
                emptyMap<String, Any>()
            } catch (e: Exception) {
                emptyMap()
            }
        },
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun Emergency.toEntity(
    syncStatus: SyncStatus = SyncStatus.PENDING,
    localId: String? = null,
    needsUpload: Boolean = false
): EmergencyEntity {
    return EmergencyEntity(
        id = this.id,
        userId = this.userId,
        emergencyType = this.emergencyTypeString,
        status = this.statusString,
        latitude = this.latitude,
        longitude = this.longitude,
        address = this.address,
        message = this.message,
        priority = this.priorityString,
        deviceInfo = this.deviceInfoMap?.let {
            // Convertir Map a JSON string
            try {
                // Usar tu parser JSON preferido
                "{}"
            } catch (e: Exception) {
                "{}"
            }
        },
        responseTime = this.responseTime,
        responderInfo = this.responderInfoMap?.let {
            try {
                "{}"
            } catch (e: Exception) {
                "{}"
            }
        },
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        syncStatus = syncStatus.name,
        localId = localId,
        needsUpload = needsUpload
    )
}