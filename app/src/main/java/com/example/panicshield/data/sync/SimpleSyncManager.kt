package com.example.panicshield.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.panicshield.data.local.dao.EmergencyHistoryDao
import com.example.panicshield.data.local.entity.EmergencyHistoryCacheEntity
import com.example.panicshield.data.remote.repository.EmergencyRepository
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.domain.mapper.toCacheEntity
import com.example.panicshield.domain.model.Emergency
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SimpleSyncManager @Inject constructor(
    private val localDao: EmergencyHistoryDao,
    private val emergencyRepository: EmergencyRepository,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "SimpleSyncManager"
    }

    suspend fun syncIfConnected(userId: String? = null): SyncResult {
        if (!isNetworkAvailable()) {
            return SyncResult.NoConnection
        }

        return try {
            Log.d(TAG, "Iniciando sincronización...")

            // 1. Obtener datos remotos
            val remoteResult = emergencyRepository.getEmergencyHistory()

            if (remoteResult.isSuccess) {
                val remoteEmergencies = remoteResult.getOrNull() ?: emptyList()
                Log.d(TAG, "Obtenidos ${remoteEmergencies.size} registros remotos")

                // 2. Obtener datos locales existentes (filtrar por usuario si se especifica)
                val localEmergencies = if (userId != null) {
                    localDao.getHistoryByUser(userId)
                } else {
                    // Si no hay filtro de usuario, obtener todos los registros
                    // Necesitarías agregar este método al DAO
                    localDao.getAllEmergencies()
                }
                Log.d(TAG, "Encontrados ${localEmergencies.size} registros locales")

                // 3. Realizar sincronización inteligente
                val syncStats = performIntelligentSync(remoteEmergencies, localEmergencies)

                Log.d(TAG, "Sincronización completada: $syncStats")
                return SyncResult.Success(syncStats)
            } else {
                Log.e(TAG, "Error al obtener datos remotos")
                return SyncResult.Error("Error al obtener datos remotos")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la sincronización", e)
            return SyncResult.Error(e.message ?: "Error desconocido")
        }
    }

    private suspend fun performIntelligentSync(
        remoteEmergencies: List<Emergency>, // Tipo correcto del dominio
        localEmergencies: List<EmergencyHistoryCacheEntity>
    ): SyncStats {
        val currentTime = System.currentTimeMillis()
        var inserted = 0
        var updated = 0
        var unchanged = 0

        // Convertir datos remotos a entidades locales
        val remoteCacheEntities = remoteEmergencies.map { emergency ->
            emergency.toCacheEntity()
        }

        // Crear mapa de datos locales para búsqueda rápida por ID
        val localMap = localEmergencies.associateBy { it.id }

        for (remoteEntity in remoteCacheEntities) {
            val localEntity = localMap[remoteEntity.id]

            when {
                localEntity == null -> {
                    // Nuevo registro - insertar
                    localDao.insert(remoteEntity.copy(
                        lastSyncedAt = currentTime,
                        needsSync = false
                    ))
                    inserted++
                    Log.d(TAG, "Insertado nuevo registro: ${remoteEntity.id}")
                }

                needsUpdate(localEntity, remoteEntity) -> {
                    // Registro existente pero diferente - actualizar
                    localDao.insert(remoteEntity.copy(
                        lastSyncedAt = currentTime,
                        needsSync = false
                    ))
                    updated++
                    Log.d(TAG, "Actualizado registro: ${remoteEntity.id}")
                }

                else -> {
                    // Registro sin cambios - solo actualizar timestamp de sync
                    localDao.markAsSynced(remoteEntity.id, currentTime)
                    unchanged++
                }
            }
        }

        // Opcional: Manejar registros que existen localmente pero no remotamente
        handleLocalOnlyRecords(localEmergencies, remoteCacheEntities)

        return SyncStats(inserted, updated, unchanged)
    }

    private fun needsUpdate(
        localEntity: EmergencyHistoryCacheEntity,
        remoteEntity: EmergencyHistoryCacheEntity
    ): Boolean {
        return try {
            // Comparar campos clave para determinar si hay cambios
            localEntity.status != remoteEntity.status ||
                    localEntity.emergencyType != remoteEntity.emergencyType ||
                    localEntity.updatedAt != remoteEntity.updatedAt ||
                    localEntity.address != remoteEntity.address ||
                    localEntity.message != remoteEntity.message ||
                    localEntity.priority != remoteEntity.priority ||
                    localEntity.responseTime != remoteEntity.responseTime ||
                    localEntity.latitude != remoteEntity.latitude ||
                    localEntity.longitude != remoteEntity.longitude ||
                    localEntity.deviceInfo != remoteEntity.deviceInfo
        } catch (e: Exception) {
            Log.w(TAG, "Error comparando entidades ${localEntity.id}, asumiendo actualización necesaria", e)
            true
        }
    }

    private suspend fun handleLocalOnlyRecords(
        localEmergencies: List<EmergencyHistoryCacheEntity>,
        remoteEmergencies: List<EmergencyHistoryCacheEntity>
    ) {
        val remoteIds = remoteEmergencies.map { it.id }.toSet()

        localEmergencies.forEach { localEntity ->
            if (localEntity.id !in remoteIds) {
                Log.d(TAG, "Registro local ${localEntity.id} no encontrado en remoto")

                // Si el registro necesita sincronización, probablemente fue creado localmente
                if (localEntity.needsSync) {
                    Log.d(TAG, "Registro local ${localEntity.id} pendiente de subir al servidor")
                    // Aquí podrías intentar subirlo al servidor
                    // uploadLocalRecord(localEntity)
                } else {
                    // El registro existía en el servidor pero ya no está
                    // Podrías marcarlo como eliminado o dejarlo como está
                    Log.w(TAG, "Registro ${localEntity.id} eliminado del servidor")
                }
            }
        }
    }

    suspend fun syncPendingToServer(): SyncResult {
        if (!isNetworkAvailable()) {
            return SyncResult.NoConnection
        }

        return try {
            val pendingItems = localDao.getPendingSyncItems()
            Log.d(TAG, "Sincronizando ${pendingItems.size} registros pendientes al servidor")

            var uploaded = 0
            var failed = 0

            for (item in pendingItems) {
                try {
                    // Convertir EmergencyHistoryCacheEntity a los parámetros necesarios para crear emergencia
                    val result = emergencyRepository.createEmergency(
                        emergencyType = item.emergencyType,
                        status = item.status,
                        latitude = item.latitude,
                        longitude = item.longitude,
                        address = item.address,
                        message = item.message,
                        priority = item.priority ?: "HIGH",
                        deviceInfo = item.deviceInfo?.let {
                            // Convertir deviceInfo de String a Map si es necesario
                            mapOf("info" to it)
                        }
                    )

                    if (result.isSuccess) {
                        localDao.markAsSynced(item.id, System.currentTimeMillis())
                        uploaded++
                        Log.d(TAG, "Registro ${item.id} subido exitosamente")
                    } else {
                        failed++
                        Log.e(TAG, "Error subiendo registro ${item.id}: ${result.exceptionOrNull()?.message}")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error subiendo registro ${item.id}", e)
                    failed++
                }
            }

            val stats = SyncStats(uploaded, 0, failed)
            Log.d(TAG, "Subida completada: $uploaded exitosos, $failed fallidos")
            return SyncResult.Success(stats)

        } catch (e: Exception) {
            Log.e(TAG, "Error sincronizando registros pendientes", e)
            return SyncResult.Error(e.message ?: "Error subiendo registros")
        }
    }

    suspend fun fullSync(userId: String? = null): SyncResult {
        Log.d(TAG, "Iniciando sincronización completa...")

        return try {
            // 1. Primero subir registros pendientes
            val uploadResult = syncPendingToServer()

            // 2. Luego descargar datos del servidor
            val downloadResult = syncIfConnected(userId)

            when {
                downloadResult is SyncResult.Success -> downloadResult
                uploadResult is SyncResult.Error -> uploadResult
                else -> downloadResult
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización completa", e)
            SyncResult.Error(e.message ?: "Error en sincronización completa")
        }
    }

    suspend fun forceSyncAll(): SyncResult {
        Log.d(TAG, "Forzando sincronización completa (limpiando cache local)...")

        return try {
            // Limpiar datos locales y recargar todo desde remoto
            localDao.clearAll()
            syncIfConnected()
        } catch (e: Exception) {
            Log.e(TAG, "Error en sincronización forzada", e)
            SyncResult.Error(e.message ?: "Error en sincronización forzada")
        }
    }

    suspend fun getLastSyncTime(): Long {
        return localDao.getLastSyncTimestamp() ?: 0L
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

// Clases de datos para manejar resultados de sincronización
sealed class SyncResult {
    object NoConnection : SyncResult()
    data class Success(val stats: SyncStats) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

data class SyncStats(
    val inserted: Int,
    val updated: Int,
    val unchanged: Int
) {
    val total: Int get() = inserted + updated + unchanged

    override fun toString(): String {
        return "Insertados: $inserted, Actualizados: $updated, Sin cambios: $unchanged, Total: $total"
    }
}