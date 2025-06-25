package com.example.panicshield.data.remote.dto

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

// ===== ENUMS PARA MANEJO DE SINCRONIZACIÓN =====

enum class SyncStatus {
    SYNCED,           // Sincronizado correctamente
    PENDING,          // Pendiente de sincronización
    FAILED,           // Falló la sincronización
    CONFLICT,         // Conflicto detectado
    LOCAL_ONLY        // Solo existe localmente
}

enum class SyncOperation {
    CREATE,           // Crear en servidor
    UPDATE,           // Actualizar en servidor
    DELETE,           // Eliminar en servidor
    NONE              // Sin operación pendiente
}

// ===== DTO PRINCIPAL CON CAMPOS DE SINCRONIZACIÓN =====

data class EmergencyDto(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @SerializedName("user_id")
    val userId: String? = null,

    @SerializedName("emergency_type")
    val emergencyType: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("priority")
    val priority: String? = null,

    @SerializedName("device_info")
    @JsonAdapter(JsonMapDeserializer::class)
    val deviceInfo: Map<String, Any>? = null,

    @SerializedName("response_time")
    val responseTime: Int? = null,

    @SerializedName("responder_info")
    @JsonAdapter(JsonMapDeserializer::class)
    val responderInfo: Map<String, Any>? = null,

    // ===== CAMPOS DE SINCRONIZACIÓN HÍBRIDA =====

    // ID temporal para registros locales antes de sincronizar
    val localId: String? = null,

    // Estado de sincronización
    val syncStatus: SyncStatus = SyncStatus.SYNCED,

    // Operación pendiente de sincronización
    val pendingOperation: SyncOperation = SyncOperation.NONE,

    // Timestamp de última sincronización exitosa
    val lastSyncTimestamp: Long? = null,

    // Timestamp de última modificación local
    val localModifiedAt: Long = System.currentTimeMillis(),

    // Número de reintentos de sincronización
    val syncRetryCount: Int = 0,

    // Hash de los datos para detección de cambios
    val dataHash: String? = null,

    // Versión para control de concurrencia optimista
    val version: Int = 1,

    // Metadatos de sincronización
    val syncMetadata: Map<String, Any>? = null,

    // Indica si el registro fue creado offline
    val isOfflineCreated: Boolean = false,

    // Timestamp de creación local (independiente del servidor)
    val localCreatedAt: Long = System.currentTimeMillis(),

    // Errores de sincronización
    val syncErrors: List<String>? = null,

    // Indica si hay conflictos pendientes de resolución
    val hasConflicts: Boolean = false,

    // Datos del servidor en caso de conflicto
    val serverData: Map<String, Any>? = null
) {

    // ===== MÉTODOS DE UTILIDAD =====

    /**
     * Verifica si el registro necesita sincronización
     */
    fun needsSync(): Boolean {
        return syncStatus in listOf(
            SyncStatus.PENDING,
            SyncStatus.FAILED
        ) && pendingOperation != SyncOperation.NONE
    }

    /**
     * Verifica si el registro está sincronizado
     */
    fun isSynced(): Boolean {
        return syncStatus == SyncStatus.SYNCED && !hasConflicts
    }

    /**
     * Verifica si puede ser sincronizado (no excede reintentos máximos)
     */
    fun canRetrySync(maxRetries: Int = 3): Boolean {
        return syncRetryCount < maxRetries
    }

    /**
     * Crea una copia marcada para sincronización
     */
    fun markForSync(operation: SyncOperation): EmergencyDto {
        return this.copy(
            syncStatus = SyncStatus.PENDING,
            pendingOperation = operation,
            localModifiedAt = System.currentTimeMillis(),
            syncErrors = null
        )
    }

    /**
     * Marca como sincronizado exitosamente
     */
    fun markAsSynced(serverId: Long? = null): EmergencyDto {
        return this.copy(
            id = serverId ?: this.id,
            syncStatus = SyncStatus.SYNCED,
            pendingOperation = SyncOperation.NONE,
            lastSyncTimestamp = System.currentTimeMillis(),
            syncRetryCount = 0,
            syncErrors = null,
            hasConflicts = false,
            serverData = null
        )
    }

    /**
     * Marca como falló la sincronización
     */
    fun markSyncFailed(error: String): EmergencyDto {
        return this.copy(
            syncStatus = SyncStatus.FAILED,
            syncRetryCount = syncRetryCount + 1,
            syncErrors = (syncErrors ?: emptyList()) + error
        )
    }

    /**
     * Marca como conflicto detectado
     */
    fun markAsConflict(serverData: Map<String, Any>): EmergencyDto {
        return this.copy(
            syncStatus = SyncStatus.CONFLICT,
            hasConflicts = true,
            serverData = serverData
        )
    }

    /**
     * Genera hash de los datos principales para detección de cambios
     */
    fun generateDataHash(): String {
        val dataString = "${emergencyType}${status}${latitude}${longitude}" +
                "${address}${message}${priority}${deviceInfo}${responseTime}${responderInfo}"
        return dataString.hashCode().toString()
    }

    /**
     * Crea copia con hash actualizado
     */
    fun withUpdatedHash(): EmergencyDto {
        return this.copy(dataHash = generateDataHash())
    }
}

// ===== DTO PARA CREACIÓN CON CAMPOS DE SINCRONIZACIÓN =====

data class CreateEmergencyDto(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("emergency_type")
    val emergencyType: String,

    @SerializedName("status")
    val status: String = "pending",

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("priority")
    val priority: String = "HIGH",

    @SerializedName("device_info")
    val deviceInfo: Map<String, Any>? = null,

    // ===== CAMPOS DE SINCRONIZACIÓN =====

    // ID local temporal (no se envía al servidor)
    val localId: String = java.util.UUID.randomUUID().toString(),

    // Timestamp de creación local
    val localCreatedAt: Long = System.currentTimeMillis(),

    // Metadatos de creación offline
    val creationMetadata: Map<String, Any>? = null,

    // Indica si fue creado offline
    val isOfflineCreated: Boolean = false
) {

    /**
     * Convierte a EmergencyDto para almacenamiento local
     */
    fun toEmergencyDto(): EmergencyDto {
        return EmergencyDto(
            id = null, // Se asignará cuando se sincronice
            userId = userId,
            emergencyType = emergencyType,
            status = status,
            latitude = latitude,
            longitude = longitude,
            address = address,
            message = message,
            priority = priority,
            deviceInfo = deviceInfo,
            localId = localId,
            syncStatus = if (isOfflineCreated) SyncStatus.PENDING else SyncStatus.SYNCED,
            pendingOperation = if (isOfflineCreated) SyncOperation.CREATE else SyncOperation.NONE,
            localCreatedAt = localCreatedAt,
            localModifiedAt = localCreatedAt,
            isOfflineCreated = isOfflineCreated,
            syncMetadata = creationMetadata
        ).withUpdatedHash()
    }
}

// ===== DTO PARA ACTUALIZACIÓN CON CAMPOS DE SINCRONIZACIÓN =====

data class UpdateEmergencyDto(
    @SerializedName("emergency_type")
    val emergencyType: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("address")
    val address: String? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("priority")
    val priority: String? = null,

    @SerializedName("device_info")
    val deviceInfo: Map<String, Any>? = null,

    @SerializedName("response_time")
    val responseTime: Int? = null,

    @SerializedName("responder_info")
    val responderInfo: Map<String, Any>? = null,

    // ===== CAMPOS DE SINCRONIZACIÓN =====

    // Versión esperada para control de concurrencia
    val expectedVersion: Int? = null,

    // Timestamp de modificación local
    val localModifiedAt: Long = System.currentTimeMillis(),

    // Metadatos de la actualización
    val updateMetadata: Map<String, Any>? = null,

    // Indica si fue modificado offline
    val isOfflineModified: Boolean = false
) {

    /**
     * Aplica la actualización a un EmergencyDto existente
     */
    fun applyTo(existing: EmergencyDto): EmergencyDto {
        return existing.copy(
            emergencyType = emergencyType ?: existing.emergencyType,
            status = status ?: existing.status,
            latitude = latitude ?: existing.latitude,
            longitude = longitude ?: existing.longitude,
            address = address ?: existing.address,
            message = message ?: existing.message,
            priority = priority ?: existing.priority,
            deviceInfo = deviceInfo ?: existing.deviceInfo,
            responseTime = responseTime ?: existing.responseTime,
            responderInfo = responderInfo ?: existing.responderInfo,

            // Actualizar campos de sincronización
            syncStatus = if (isOfflineModified) SyncStatus.PENDING else existing.syncStatus,
            pendingOperation = if (isOfflineModified) SyncOperation.UPDATE else existing.pendingOperation,
            localModifiedAt = localModifiedAt,
            version = existing.version + 1,
            syncMetadata = updateMetadata ?: existing.syncMetadata
        ).withUpdatedHash()
    }
}

// ===== DTO PARA RESPUESTA DE SINCRONIZACIÓN =====

data class SyncResponseDto(
    val success: Boolean,
    val syncedItems: List<EmergencyDto> = emptyList(),
    val conflicts: List<ConflictDto> = emptyList(),
    val errors: List<SyncErrorDto> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class ConflictDto(
    val localId: String,
    val serverId: Long?,
    val localData: EmergencyDto,
    val serverData: EmergencyDto,
    val conflictType: String, // "version", "data", "delete"
    val conflictFields: List<String> = emptyList()
)

data class SyncErrorDto(
    val localId: String,
    val serverId: Long?,
    val operation: SyncOperation,
    val errorType: String,
    val errorMessage: String,
    val retryable: Boolean = true
)

// ===== DTO PARA BATCH DE SINCRONIZACIÓN =====

data class SyncBatchDto(
    val creates: List<CreateEmergencyDto> = emptyList(),
    val updates: List<Pair<Long, UpdateEmergencyDto>> = emptyList(),
    val deletes: List<Long> = emptyList(),
    val lastSyncTimestamp: Long? = null,
    val clientTimestamp: Long = System.currentTimeMillis()
)