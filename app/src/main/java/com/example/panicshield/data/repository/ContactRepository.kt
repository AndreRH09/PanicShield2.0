package com.example.panicshield.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.local.dao.ContactDao
import com.example.panicshield.data.remote.api.ContactApi
import com.example.panicshield.data.remote.dto.CreateContactDto
import com.example.panicshield.data.remote.dto.UpdateContactDto
import com.example.panicshield.domain.model.Contact
import com.example.panicshield.domain.model.PhoneContact
import com.example.panicshield.utils.Resource
import com.example.panicshield.utils.networkBoundResource
import com.example.panicshield.data.mapper.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val contactApi: ContactApi,
    private val contactDao: ContactDao,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) {

    // Evaluar isDataStale() antes del networkBoundResource
    suspend fun getContacts(): Flow<Resource<List<Contact>>> {
        val userId = tokenManager.getUserId().first()
            ?: throw Exception("No user ID available")

        val token = tokenManager.getAccessToken().first()
            ?: throw Exception("No token available")

        // Evaluar si los datos están obsoletos ANTES de networkBoundResource
        val dataIsStale = isDataStale()

        return networkBoundResource(
            query = {
                contactDao.getContactsByUserId(userId).map { entities ->
                    entities.toDomainList()
                }
            },
            fetch = {
                val response = contactApi.getContacts(
                    authorization = "Bearer $token",
                    userId = "eq.$userId"
                )

                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    throw Exception("Failed to fetch contacts: ${response.message()}")
                }
            },
            saveFetchResult = { contactDtos ->
                contactDao.deleteAllByUserId(userId)
                contactDao.insertAll(contactDtos.toEntityList(userId))
            },
            shouldFetch = { contacts ->
                // Usar la variable evaluada previamente
                contacts.isEmpty() || dataIsStale
            }
        )
    }
    // Crear contacto individual
    suspend fun createContact(name: String, phone: String): Result<Contact> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val userId = tokenManager.getUserId().first()
                ?: return Result.failure(Exception("No user ID available"))

            val createContactDto = CreateContactDto(
                userId = userId,
                name = name,
                phone = phone
            )

            val response = contactApi.createContact(
                authorization = "Bearer $token",
                contact = createContactDto
            )

            if (response.isSuccessful) {
                val createdContactDto = response.body()?.firstOrNull()
                    ?: return Result.failure(Exception("Failed to create contact"))

                // Guardar en BD local
                val contactEntity = createdContactDto.toEntity(userId)
                contactDao.insert(contactEntity)

                Result.success(contactEntity.toDomain())
            } else {
                Result.failure(Exception("Failed to create contact: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar contacto
    suspend fun updateContact(id: Long, name: String, phone: String): Result<Contact> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val updateContactDto = UpdateContactDto(name = name, phone = phone)

            val response = contactApi.updateContact(
                authorization = "Bearer $token",
                id = "eq.$id",
                contact = updateContactDto
            )

            if (response.isSuccessful) {
                val updatedContactDto = response.body()?.firstOrNull()
                    ?: return Result.failure(Exception("Failed to update contact"))

                // Actualizar en BD local
                val userId = tokenManager.getUserId().first() ?: ""
                val contactEntity = updatedContactDto.toEntity(userId)
                contactDao.update(contactEntity)

                Result.success(contactEntity.toDomain())
            } else {
                Result.failure(Exception("Failed to update contact: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar contacto
    suspend fun deleteContact(id: Long): Result<Unit> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val response = contactApi.deleteContact(
                authorization = "Bearer $token",
                id = "eq.$id"
            )

            if (response.isSuccessful) {
                // Eliminar de BD local
                val contactEntity = contactDao.getContactById(id)
                contactEntity?.let { contactDao.delete(it) }

                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete contact: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Contactos del teléfono (sin cambios)
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

                while (it.moveToNext()) {
                    val name = it.getString(nameColumnIndex) ?: ""
                    val phone = it.getString(phoneColumnIndex) ?: ""

                    if (name.isNotBlank() && phone.isNotBlank()) {
                        contacts.add(PhoneContact(name = name, phone = phone))
                    }
                }
            }
        } catch (e: Exception) {
            // Manejar errores de permisos
        }

        return contacts.distinctBy { it.phone }
    }

    // Función para verificar si los datos están obsoletos
    private suspend fun isDataStale(): Boolean {
        val threshold = 5 * 60 * 1000 // 5 minutos
        val currentTime = System.currentTimeMillis()

        val userId = tokenManager.getUserId().first() ?: return true
        val contacts = contactDao.getContactsByUserId(userId).first()

        return if (contacts.isEmpty()) {
            true
        } else {
            contacts.any { (currentTime - it.lastFetchedTime) > threshold }
        }
    }

    // Forzar sincronización
    suspend fun syncContacts(): Flow<Resource<List<Contact>>> {
        val userId = tokenManager.getUserId().first()
            ?: throw Exception("No user ID available")

        val token = tokenManager.getAccessToken().first()
            ?: throw Exception("No token available")

        return networkBoundResource(
            query = {
                contactDao.getContactsByUserId(userId).map { entities ->
                    entities.toDomainList()
                }
            },
            fetch = {
                val response = contactApi.getContacts(
                    authorization = "Bearer $token",
                    userId = "eq.$userId"
                )

                if (response.isSuccessful) {
                    response.body() ?: emptyList()
                } else {
                    throw Exception("Failed to sync contacts: ${response.message()}")
                }
            },
            saveFetchResult = { contactDtos ->
                contactDao.deleteAllByUserId(userId)
                contactDao.insertAll(contactDtos.toEntityList(userId))
            },
            shouldFetch = { true } // Siempre sincronizar cuando se llama explícitamente
        )
    }
}
