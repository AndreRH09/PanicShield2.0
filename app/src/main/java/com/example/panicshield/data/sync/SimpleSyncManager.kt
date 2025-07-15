package com.example.panicshield.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.panicshield.data.local.dao.EmergencyHistoryDao
import com.example.panicshield.data.remote.repository.EmergencyRepository
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.domain.mapper.toCacheEntity
import dagger.hilt.android.qualifiers.ApplicationContext // AGREGAR ESTE IMPORT
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

    suspend fun syncIfConnected(): Boolean {
        if (!isNetworkAvailable()) {
            return false
        }

        return try {
            // 1. Obtener datos remotos
            val remoteResult = emergencyRepository.getEmergencyHistory()

            if (remoteResult.isSuccess) {
                val remoteEmergencies = remoteResult.getOrNull() ?: emptyList()

                // 2. Convertir y guardar en cache local
                val cacheEntities = remoteEmergencies.map { emergency ->
                    emergency.toCacheEntity()
                }

                localDao.insertAll(cacheEntities)

                // 3. Marcar timestamp de sincronización
                val currentTime = System.currentTimeMillis()
                cacheEntities.forEach { entity ->
                    localDao.markAsSynced(entity.id, currentTime)
                }

                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}