package com.example.panicshield.data.remote.repository

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.local.dao.ContactDao
import com.example.panicshield.data.local.entity.toDomain
import com.example.panicshield.data.local.entity.toEntity
import com.example.panicshield.data.remote.api.ContactApi
import com.example.panicshield.data.remote.dto.CreateContactDto
import com.example.panicshield.data.remote.dto.UpdateContactDto
import com.example.panicshield.data.util.Resource
import com.example.panicshield.data.util.networkBoundResource
import com.example.panicshield.domain.model.Contact
import com.example.panicshield.domain.model.PhoneContact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

    /**
     * obtiene los contactos usando el patrón NetworkBoundResource
     */
    fun getContacts(): Flow<Resource<List<Contact>>> {
        return tokenManager.getUserId().flatMapLatest { userId ->
            if (userId != null) {
                networkBoundResource(
                    query = {
                        // Obtenemos los contactos de la base de datos local
                        contactDao.getContactsByUserId(userId).map { entities ->
                            entities.map { it.toDomain() }
                        }
                    },
                    fetch = {
                        // Obtenemos datos de la API
                        fetchContactsFromApi()
                    },
                    saveFetchResult = { contacts ->
                        // Guardamos en la base de datos local
                        saveContactsToLocal(contacts)
                    },
                    shouldFetch = { localContacts ->
                        // Decidimos si necesitamos hacer petición a la red
                        // Para hacer esto no-suspend, podemos usar una verificación más simple
                        shouldFetchFromNetworkNonSuspend(localContacts, userId)
                    }
                )
            } else {
                flowOf(Resource.Error("No user ID available"))
            }
        }
    }

    /**
     * Obtiene contactos directamente desde la API (para uso interno)
     */
    private suspend fun fetchContactsFromApi(): List<Contact> {
        val token = tokenManager.getAccessToken().first()
            ?: throw Exception("No token available")

        val userId = tokenManager.getUserId().first()
            ?: throw Exception("No user ID available")

        val response = contactApi.getContacts(
            authorization = "Bearer $token",
            userId = "eq.$userId"
        )

        if (response.isSuccessful) {
            return response.body()?.map { dto ->
                Contact(
                    id = dto.id,
                    createdAt = dto.createdAt,
                    userId = dto.userId ?: userId,
                    name = dto.name,
                    phone = dto.phone
                )
            } ?: emptyList()
        } else {
            throw Exception("Failed to get contacts: ${response.message()}")
        }
    }

    /**
     * Guarda contactos en la base de datos local
     */
    private suspend fun saveContactsToLocal(contacts: List<Contact>) {
        val userId = tokenManager.getUserId().first()
            ?: throw Exception("No user ID available")

        // Convertimos a entidades y guardamos
        val entities = contacts.map { it.toEntity() }

        // Eliminamos los contactos existentes del usuario y insertamos los nuevos
        contactDao.deleteAllContactsByUserId(userId)
        contactDao.insertContacts(entities)
    }

    /**
     * Versión no-suspend para determinar si debemos hacer petición a la red
     * Esta se usa en el shouldFetch callback que no puede ser suspend
     */
    private fun shouldFetchFromNetworkNonSuspend(localContacts: List<Contact>, userId: String): Boolean {
        // Si no hay datos locales, siempre hacer petición
        if (localContacts.isEmpty()) {
            return true
        }

        // Para una verificación más simple sin suspend, podemos:
        // 1. Siempre retornar false si hay datos (no refrescar automáticamente)
        // 2. O implementar una lógica más compleja con timestamps en las entidades

        // Opción simple: solo refrescar si no hay datos
        return false
    }

    /**
     * Función suspend alternativa para forzar refresh desde la UI
     */
    suspend fun shouldRefreshContacts(): Boolean {
        val userId = tokenManager.getUserId().first() ?: return false

        // Si han pasado más de 5 minutos desde la última actualización
        val lastUpdateTime = contactDao.getLastUpdateTime(userId) ?: 0L
        val fiveMinutesInMillis = 5 * 60 * 1000L
        val shouldUpdate = System.currentTimeMillis() - lastUpdateTime > fiveMinutesInMillis

        return shouldUpdate
    }

    /**
     * Función para forzar refresh desde la UI
     */
    suspend fun refreshContacts(): Flow<Resource<List<Contact>>> {
        return tokenManager.getUserId().flatMapLatest { userId ->
            if (userId != null) {
                networkBoundResource(
                    query = {
                        contactDao.getContactsByUserId(userId).map { entities ->
                            entities.map { it.toDomain() }
                        }
                    },
                    fetch = {
                        fetchContactsFromApi()
                    },
                    saveFetchResult = { contacts ->
                        saveContactsToLocal(contacts)
                    },
                    shouldFetch = { _ ->
                        // Siempre hacer fetch en refresh manual
                        true
                    }
                )
            } else {
                flowOf(Resource.Error("No user ID available"))
            }
        }
    }

    /**
     * Crea un nuevo contacto
     */
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
                val createdContact = response.body()?.firstOrNull()?.let { dto ->
                    Contact(
                        id = dto.id,
                        createdAt = dto.createdAt,
                        userId = dto.userId ?: userId,
                        name = dto.name,
                        phone = dto.phone
                    )
                } ?: return Result.failure(Exception("Failed to create contact"))

                // También guardamos en la base de datos local
                contactDao.insertContact(createdContact.toEntity())

                Result.success(createdContact)
            } else {
                Result.failure(Exception("Failed to create contact: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza un contacto existente
     */
    suspend fun updateContact(id: Long, name: String, phone: String): Result<Contact> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val updateContactDto = UpdateContactDto(
                name = name,
                phone = phone
            )

            val response = contactApi.updateContact(
                authorization = "Bearer $token",
                id = "eq.$id",
                contact = updateContactDto
            )

            if (response.isSuccessful) {
                val updatedContact = response.body()?.firstOrNull()?.let { dto ->
                    Contact(
                        id = dto.id,
                        createdAt = dto.createdAt,
                        userId = dto.userId ?: "",
                        name = dto.name,
                        phone = dto.phone
                    )
                } ?: return Result.failure(Exception("Failed to update contact"))

                // También actualizamos en la base de datos local
                contactDao.updateContact(updatedContact.toEntity())

                Result.success(updatedContact)
            } else {
                Result.failure(Exception("Failed to update contact: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un contacto
     */
    suspend fun deleteContact(id: Long): Result<Unit> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val response = contactApi.deleteContact(
                authorization = "Bearer $token",
                id = "eq.$id"
            )

            if (response.isSuccessful) {
                // También eliminamos de la base de datos local
                contactDao.deleteContactById(id)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete contact: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene contactos del teléfono
     */
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
            Log.e("ContactRepository", "Error getting phone contacts", e)
        }

        return contacts.distinctBy { it.phone }
    }
}