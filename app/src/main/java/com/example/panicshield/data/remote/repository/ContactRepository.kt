package com.example.panicshield.data.remote.repository

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.example.panicshield.data.local.TokenManager
import com.example.panicshield.data.remote.api.ContactApi
import com.example.panicshield.data.remote.dto.CreateContactDto
import com.example.panicshield.data.remote.dto.UpdateContactDto
import com.example.panicshield.domain.model.Contact
import com.example.panicshield.domain.model.PhoneContact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val contactApi: ContactApi,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) {

    suspend fun getContacts(): Result<List<Contact>> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val userId = tokenManager.getUserId().first()
                ?: return Result.failure(Exception("No user ID available"))

            val response = contactApi.getContacts(
                authorization = "Bearer $token",
                userId = "eq.$userId"
            )

            if (response.isSuccessful) {
                val contacts = response.body()?.map { dto ->
                    Contact(
                        id = dto.id,
                        createdAt = dto.createdAt,
                        userId = dto.userId ?: userId,
                        name = dto.name,
                        phone = dto.phone
                    )
                } ?: emptyList()
                Result.success(contacts)
            } else {
                Result.failure(Exception("Failed to get contacts: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

                Result.success(createdContact)
            } else {
                Result.failure(Exception("Failed to create contact: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

                Result.success(updatedContact)
            } else {
                Result.failure(Exception("Failed to update contact: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteContact(id: Long): Result<Unit> {
        return try {
            val token = tokenManager.getAccessToken().first()
                ?: return Result.failure(Exception("No token available"))

            val response = contactApi.deleteContact(
                authorization = "Bearer $token",
                id = "eq.$id"
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete contact: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
            // Manejar errores de permisos o acceso a contactos
        }

        return contacts.distinctBy { it.phone }
    }
}