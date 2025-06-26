package com.example.panicshield.domain.usecase

import com.example.panicshield.data.remote.repository.ContactRepository
import com.example.panicshield.data.util.Resource
import com.example.panicshield.domain.model.Contact
import com.example.panicshield.domain.model.PhoneContact
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {

    /**
     * Obtiene todos los contactos usando el patrón NetworkBoundResource
     */
    fun getContacts(): Flow<Resource<List<Contact>>> {
        return contactRepository.getContacts()
    }

    /**
     * Crea un nuevo contacto
     */
    suspend fun createContact(name: String, phone: String): Result<Contact> {
        return try {
            // Validaciones de negocio
            if (name.isBlank()) {
                return Result.failure(Exception("El nombre no puede estar vacío"))
            }

            if (phone.isBlank()) {
                return Result.failure(Exception("El teléfono no puede estar vacío"))
            }

            // Validar formato de teléfono (ejemplo básico)
            val cleanPhone = phone.replace(Regex("[^\\d+]"), "")
            if (cleanPhone.length < 9) {
                return Result.failure(Exception("El teléfono debe tener al menos 9 dígitos"))
            }

            contactRepository.createContact(name.trim(), cleanPhone)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza un contacto existente
     */
    suspend fun updateContact(id: Long, name: String, phone: String): Result<Contact> {
        return try {
            // Validaciones de negocio
            if (name.isBlank()) {
                return Result.failure(Exception("El nombre no puede estar vacío"))
            }

            if (phone.isBlank()) {
                return Result.failure(Exception("El teléfono no puede estar vacío"))
            }

            // Validar formato de teléfono
            val cleanPhone = phone.replace(Regex("[^\\d+]"), "")
            if (cleanPhone.length < 9) {
                return Result.failure(Exception("El teléfono debe tener al menos 9 dígitos"))
            }

            contactRepository.updateContact(id, name.trim(), cleanPhone)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un contacto
     */
    suspend fun deleteContact(id: Long): Result<Unit> {
        return try {
            contactRepository.deleteContact(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los contactos del teléfono
     */
    fun getPhoneContacts(): List<PhoneContact> {
        return contactRepository.getPhoneContacts()
    }

    /**
     * Fuerza la sincronización con el servidor
     */
    fun forceRefresh(): Flow<Resource<List<Contact>>> {
        return contactRepository.getContacts()
    }
}