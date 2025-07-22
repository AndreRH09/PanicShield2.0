// data/remote/repository/ContactRepository.kt
package com.example.panicshield.data.remote.repository

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.asFlow
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.local.dao.ContactDao
import com.example.panicshield.data.local.entity.ContactEntity
import com.example.panicshield.data.sync.SyncConfiguration
import com.example.panicshield.domain.mapper.ContactMapper.toDomain
import com.example.panicshield.domain.mapper.ContactMapper.toEntity
import com.example.panicshield.domain.model.Contact
import com.example.panicshield.domain.model.PhoneContact
import com.example.supabaseofflinesupport.OfflineCrudType
import com.example.supabaseofflinesupport.SyncManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val contactDao: ContactDao,
    private val tokenManager: TokenManager,
    private val syncManager: SyncManager,
    private val syncConfiguration: SyncConfiguration,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "ContactRepository"
    }

    // Observar contactos desde base de datos local
    fun getContactsFlow(userId: String): Flow<List<Contact>> {
        return contactDao.getContactsByUserId(userId).map { entities ->
            entities.filter { it.offlineFieldOpType != OfflineCrudType.DELETE.ordinal }
                .map { it.toDomain() }
        }
    }

    // Obtener contactos sincronizados
    suspend fun getContacts(): Result<List<Contact>> {
        return try {
            val userId = tokenManager.getUserId().first()
                ?: return Result.failure(Exception("No user ID available"))

            // Intentar sincronizar si hay conexión
            if (syncManager.isNetworkAvailable()) {
                try {
                    syncContacts()
                } catch (e: Exception) {
                    Log.w(TAG, "Sync failed but continuing with local data", e)
                }
            }

            // Obtener desde base de datos local (excluir eliminados)
            val contacts = contactDao.getContactsByUserIdSync(userId)
                .filter { it.offlineFieldOpType != OfflineCrudType.DELETE.ordinal }
                .map { it.toDomain() }

            Result.success(contacts)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting contacts", e)
            Result.failure(e)
        }
    }

    // Crear contacto (offline-first)
    suspend fun createContact(name: String, phone: String): Result<Contact> {
        return try {
            val userId = tokenManager.getUserId().first()
                ?: return Result.failure(Exception("No user ID available"))

            // Validar entrada
            if (name.isBlank()) {
                return Result.failure(Exception("Name cannot be empty"))
            }
            if (phone.isBlank()) {
                return Result.failure(Exception("Phone cannot be empty"))
            }

            // Verificar si ya existe un contacto con el mismo teléfono
            val existingContacts = contactDao.getContactsByUserIdSync(userId)
            if (existingContacts.any { it.phone == phone && it.offlineFieldOpType != OfflineCrudType.DELETE.ordinal }) {
                return Result.failure(Exception("Contact with this phone number already exists"))
            }

            // Generar ID temporal negativo para nuevos contactos
            val tempId = -System.currentTimeMillis().toInt()

            val contactEntity = ContactEntity(
                id = tempId,
                lastUpdatedTimestamp = System.currentTimeMillis(),
                offlineFieldOpType = OfflineCrudType.INSERT.ordinal,
                userId = userId,
                name = name.trim(),
                phone = phone.trim(),
                createdAt = null
            )

            contactDao.insert(contactEntity)

            // Intentar sincronizar inmediatamente si hay conexión
            if (syncManager.isNetworkAvailable()) {
                try {
                    syncContacts()
                } catch (e: Exception) {
                    Log.w(TAG, "Immediate sync failed after create", e)
                }
            }

            Result.success(contactEntity.toDomain())
        } catch (e: Exception) {
            Log.e(TAG, "Error creating contact", e)
            Result.failure(e)
        }
    }

    // Actualizar contacto (offline-first)
    suspend fun updateContact(id: Long, name: String, phone: String): Result<Contact> {
        return try {
            // Validar entrada
            if (name.isBlank()) {
                return Result.failure(Exception("Name cannot be empty"))
            }
            if (phone.isBlank()) {
                return Result.failure(Exception("Phone cannot be empty"))
            }

            val contactEntity = contactDao.getContactById(id.toInt())
                ?: return Result.failure(Exception("Contact not found"))

            // Verificar si ya existe otro contacto con el mismo teléfono
            val userId = tokenManager.getUserId().first()
                ?: return Result.failure(Exception("No user ID available"))

            val existingContacts = contactDao.getContactsByUserIdSync(userId)
            if (existingContacts.any {
                it.phone == phone.trim() &&
                it.id != id.toInt() &&
                it.offlineFieldOpType != OfflineCrudType.DELETE.ordinal
            }) {
                return Result.failure(Exception("Contact with this phone number already exists"))
            }

            val updatedEntity = contactEntity.copy(
                name = name.trim(),
                phone = phone.trim(),
                lastUpdatedTimestamp = System.currentTimeMillis(),
                offlineFieldOpType = if (contactEntity.offlineFieldOpType == OfflineCrudType.INSERT.ordinal) {
                    OfflineCrudType.INSERT.ordinal // Mantener INSERT si es nuevo
                } else {
                    OfflineCrudType.UPDATE.ordinal
                }
            )

            contactDao.update(updatedEntity)

            // Intentar sincronizar inmediatamente si hay conexión
            if (syncManager.isNetworkAvailable()) {
                try {
                    syncContacts()
                } catch (e: Exception) {
                    Log.w(TAG, "Immediate sync failed after update", e)
                }
            }

            Result.success(updatedEntity.toDomain())
        } catch (e: Exception) {
            Log.e(TAG, "Error updating contact", e)
            Result.failure(e)
        }
    }

    // Eliminar contacto (offline-first)
    suspend fun deleteContact(id: Long): Result<Unit> {
        return try {
            val contactEntity = contactDao.getContactById(id.toInt())
                ?: return Result.failure(Exception("Contact not found"))

            if (contactEntity.offlineFieldOpType == OfflineCrudType.INSERT.ordinal) {
                // Si es un contacto nuevo que no se ha sincronizado, eliminarlo directamente
                contactDao.delete(contactEntity)
            } else {
                // Marcar como eliminado para sincronización
                contactDao.updateOfflineOpType(
                    id = id.toInt(),
                    opType = OfflineCrudType.DELETE.ordinal,
                    timestamp = System.currentTimeMillis()
                )
            }

            // Intentar sincronizar inmediatamente si hay conexión
            if (syncManager.isNetworkAvailable()) {
                try {
                    syncContacts()
                } catch (e: Exception) {
                    Log.w(TAG, "Immediate sync failed after delete", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting contact", e)
            Result.failure(e)
        }
    }

    // Sincronizar contactos con Supabase
    suspend fun syncContacts(): Result<Unit> {
        return try {
            if (!syncManager.isNetworkAvailable()) {
                return Result.failure(Exception("No network connection"))
            }

            syncConfiguration.syncContacts().getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing contacts", e)
            Result.failure(e)
        }
    }

    // Obtener contactos del teléfono con mejor manejo de errores
    fun getPhoneContacts(): List<PhoneContact> {
        val contacts = mutableListOf<PhoneContact>()

        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use {
                val nameColumnIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val phoneColumnIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                if (nameColumnIndex >= 0 && phoneColumnIndex >= 0) {
                    while (it.moveToNext()) {
                        val name = it.getString(nameColumnIndex)?.trim() ?: ""
                        val phone = it.getString(phoneColumnIndex)?.trim() ?: ""

                        if (name.isNotBlank() && phone.isNotBlank()) {
                            contacts.add(PhoneContact(name = name, phone = phone))
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "No permission to read contacts", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading phone contacts", e)
        }

        return contacts.distinctBy { it.phone }
    }

    // Observar estado de la red
    fun observeNetworkState(): Flow<Boolean> {
        return syncManager.observeNetwork().asFlow()
    }

    // Limpiar datos locales (para logout)
    suspend fun clearLocalData() {
        try {
            val userId = tokenManager.getUserId().first()
            if (userId != null) {
                contactDao.deleteAllContactsByUserId(userId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local data", e)
        }
    }

    // Obtener estadísticas de sincronización
    suspend fun getSyncStats(): Map<String, Int> {
        return try {
            val userId = tokenManager.getUserId().first() ?: return emptyMap()
            val contacts = contactDao.getContactsByUserIdSync(userId)

            mapOf(
                "total" to contacts.size,
                "synced" to contacts.count { it.offlineFieldOpType == OfflineCrudType.NONE.ordinal },
                "pending_insert" to contacts.count { it.offlineFieldOpType == OfflineCrudType.INSERT.ordinal },
                "pending_update" to contacts.count { it.offlineFieldOpType == OfflineCrudType.UPDATE.ordinal },
                "pending_delete" to contacts.count { it.offlineFieldOpType == OfflineCrudType.DELETE.ordinal }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting sync stats", e)
            emptyMap()
        }
    }


}