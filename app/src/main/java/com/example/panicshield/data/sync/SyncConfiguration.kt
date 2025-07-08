// data/sync/SyncConfiguration.kt
package com.example.panicshield.data.sync

import android.content.Context
import com.example.panicshield.data.local.dao.ContactDao
import com.example.panicshield.data.local.entity.ContactEntity
import com.example.panicshield.data.remote.dto.ContactRemoteDto
import com.example.panicshield.domain.mapper.ContactMapper.toEntity
import com.example.panicshield.domain.mapper.ContactMapper.toRemoteDto
import com.example.supabaseofflinesupport.SyncManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.serializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncConfiguration @Inject constructor(
    private val syncManager: SyncManager,
    private val contactDao: ContactDao,
    @ApplicationContext private val context: Context // Add if needed
) {
    
    companion object {
        const val CONTACTS_LOCAL_TABLE = "contacts"
        const val CONTACTS_REMOTE_TABLE = "contacts"
        const val SYNC_INTERVAL_MS = 30000L // 30 segundos
    }
    
    suspend fun syncContacts(): Result<Unit> {
        return try {
            if (!syncManager.isNetworkAvailable()) {
                return Result.failure(Exception("No network connection available"))
            }
            
            syncManager.syncToSupabase(
                localTable = CONTACTS_LOCAL_TABLE,
                localDao = contactDao,
                remoteTable = CONTACTS_REMOTE_TABLE,
                toMap = { remoteDto: ContactRemoteDto -> remoteDto.toEntity() },
                toMapWithoutLocal = { entity: ContactEntity -> entity.toRemoteDto() },
                serializer = serializer(),
                currentTimeStamp = System.currentTimeMillis()
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun performFullSync(): Result<Unit> {
        return try {
            // Sincronizar contactos
            syncContacts().getOrThrow()
            
            // Aquí puedes agregar sincronización de otras entidades
            // syncEmergencies().getOrThrow()
            // syncHistory().getOrThrow()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}