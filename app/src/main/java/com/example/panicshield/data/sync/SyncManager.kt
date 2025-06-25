package com.example.panicshield.data.sync

import com.example.panicshield.data.local.dao.EmergencyDao
import com.example.panicshield.data.local.entity.EmergencyEntity
import com.example.panicshield.data.local.entity.SyncStatus
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.local.entity.toEntity
import com.example.panicshield.data.remote.api.EmergencyApi
import com.example.panicshield.data.remote.dto.CreateEmergencyDto
import com.example.panicshield.data.remote.dto.UpdateEmergencyDto
import com.example.panicshield.domain.mapper.toDomainModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String, val exception: Throwable? = null) : SyncResult()
    data class PartialSuccess(val uploaded: Int, val downloaded: Int, val errors: List<String>) : SyncResult()
}

sealed class ConflictResolution {
    object RemoteWins : ConflictResolution()
    object LocalWins : ConflictResolution()
    object Manual : ConflictResolution()
}

@Singleton
class SyncManager @Inject constructor(
    private val emergencyDao: EmergencyDao,
    private val emergencyApi: EmergencyApi,
    private val tokenManager: TokenManager
) {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.SYNCED)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    // ===== SINCRONIZACIÓN PRINCIPAL =====

    suspend fun syncAll(): SyncResult {
        return try {
            _syncStatus.value = SyncStatus.UPLOADING
            _syncProgress.value = 0f

            val uploadResult = uploadPendingEmergencies()
            _syncProgress.value = 0.5f

            val downloadResult = downloadRemoteEmergencies()
            _syncProgress.value = 1f

            _syncStatus.value = SyncStatus.SYNCED

            when {
                uploadResult is SyncResult.Success && downloadResult is SyncResult.Success ->
                    SyncResult.Success
                uploadResult is SyncResult.Error -> uploadResult
                downloadResult is SyncResult.Error -> downloadResult
                else -> {
                    val errors = mutableListOf<String>()
                    var uploaded = 0
                    var downloaded = 0

                    if (uploadResult is SyncResult.PartialSuccess) {
                        uploaded = uploadResult.uploaded
                        errors.addAll(uploadResult.errors)
                    }
                    if (downloadResult is SyncResult.PartialSuccess) {
                        downloaded = downloadResult.downloaded
                        errors.addAll(downloadResult.errors)
                    }

                    SyncResult.PartialSuccess(uploaded, downloaded, errors)
                }
            }
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.FAILED
            SyncResult.Error("Error en sincronización completa", e)
        }
    }

    suspend fun uploadPendingEmergencies(): SyncResult {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return SyncResult.Error("No hay token de autenticación")

            val pendingEmergencies = emergencyDao.getEmergenciesNeedingUpload()

            if (pendingEmergencies.isEmpty()) {
                return SyncResult.Success
            }

            var uploadedCount = 0
            val errors = mutableListOf<String>()

            for (emergency in pendingEmergencies) {
                try {
                    emergencyDao.updateSyncStatus(
                        emergency.id ?: continue,
                        SyncStatus.UPLOADING.name
                    )

                    val result = if (emergency.id == null || emergency.id <= 0) {
                        // Crear nueva emergencia
                        createRemoteEmergency(emergency, token)
                    } else {
                        // Actualizar emergencia existente
                        updateRemoteEmergency(emergency, token)
                    }

                    if (result) {
                        uploadedCount++
                        emergencyDao.updateSyncStatus(
                            emergency.id ?: continue,
                            SyncStatus.SYNCED.name,
                            System.currentTimeMillis()
                        )
                        emergencyDao.updateNeedsUpload(emergency.id, false)
                    } else {
                        emergencyDao.updateSyncStatus(
                            emergency.id ?: continue,
                            SyncStatus.FAILED.name
                        )
                        errors.add("Error al subir emergencia ${emergency.id}")
                    }

                } catch (e: Exception) {
                    emergencyDao.updateSyncStatus(
                        emergency.id ?: continue,
                        SyncStatus.FAILED.name
                    )
                    errors.add("Error al subir emergencia ${emergency.id}: ${e.message}")
                }
            }

            when {
                errors.isEmpty() -> SyncResult.Success
                uploadedCount > 0 -> SyncResult.PartialSuccess(uploadedCount, 0, errors)
                else -> SyncResult.Error("No se pudo subir ninguna emergencia", Exception(errors.joinToString()))
            }

        } catch (e: Exception) {
            SyncResult.Error("Error general en subida", e)
        }
    }

    suspend fun downloadRemoteEmergencies(): SyncResult {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return SyncResult.Error("No hay token de autenticación")

            val userId = tokenManager.getUserId().first()
                ?: return SyncResult.Error("No hay ID de usuario")

            // Obtener último timestamp de sincronización
            val lastSyncTime = emergencyDao.getLastUpdateTime(userId) ?: 0L

            val response = emergencyApi.getUserEmergencies(
                authorization = "Bearer $token",
                userId = "eq.$userId"
            )

            if (!response.isSuccessful) {
                return SyncResult.Error("Error al descargar: ${response.message()}")
            }

            val remoteEmergencies = response.body() ?: emptyList()
            var downloadedCount = 0
            val errors = mutableListOf<String>()

            for (remoteDto in remoteEmergencies) {
                try {
                    val remoteEntity = remoteDto.toDomainModel().toEntity(
                        syncStatus = SyncStatus.SYNCED,
                        needsUpload = false
                    )

                    emergencyDao.syncEmergencyFromRemote(remoteEntity)
                    downloadedCount++

                } catch (e: Exception) {
                    errors.add("Error al procesar emergencia remota ${remoteDto.id}: ${e.message}")
                }
            }

            when {
                errors.isEmpty() -> SyncResult.Success
                downloadedCount > 0 -> SyncResult.PartialSuccess(0, downloadedCount, errors)
                else -> SyncResult.Error("No se pudo descargar ninguna emergencia", Exception(errors.joinToString()))
            }

        } catch (e: Exception) {
            SyncResult.Error("Error general en descarga", e)
        }
    }

    // ===== OPERACIONES ESPECÍFICAS =====

    private suspend fun createRemoteEmergency(emergency: EmergencyEntity, token: String): Boolean {
        return try {
            val createDto = CreateEmergencyDto(
                userId = emergency.userId,
                emergencyType = emergency.emergencyType,
                status = emergency.status,
                latitude = emergency.latitude,
                longitude = emergency.longitude,
                address = emergency.address,
                message = emergency.message,
                priority = emergency.priority,
                deviceInfo = emergency.deviceInfo?.let {
                    // Convertir JSON string a Map si es necesario
                    emptyMap<String, Any>()
                }
            )

            val response = emergencyApi.createEmergency(
                authorization = "Bearer $token",
                emergency = createDto
            )

            if (response.isSuccessful) {
                val createdEmergency = response.body()?.firstOrNull()
                if (createdEmergency != null && emergency.localId != null) {
                    // Actualizar con el ID remoto
                    emergencyDao.updateWithRemoteId(
                        emergency.localId,
                        createdEmergency.id ?: 0L
                    )
                }
                true
            } else {
                false