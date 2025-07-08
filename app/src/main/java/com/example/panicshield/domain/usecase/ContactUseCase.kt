package com.example.panicshield.domain.usecase

import com.example.panicshield.data.remote.repository.ContactRepository
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
     * Obtiene todos los contactos del usuario actual
     */
    suspend fun getContacts(): Result<List<Contact>> {
        return contactRepository.getContacts()
    }

    /**
     * Observa los cambios en los contactos
     */
    fun observeContacts(userId: String): Flow<List<Contact>> {
        return contactRepository.getContactsFlow(userId)
    }

    /**
     * Crea un nuevo contacto
     */
    suspend fun createContact(name: String, phone: String): Result<Contact> {
        return try {
            val trimmedName = name.trim()
            val trimmedPhone = phone.trim()

            when {
                trimmedName.isEmpty() -> Result.failure(Exception("El nombre no puede estar vacío"))
                trimmedName.length < 2 -> Result.failure(Exception("El nombre debe tener al menos 2 caracteres"))
                trimmedPhone.isEmpty() -> Result.failure(Exception("El teléfono no puede estar vacío"))
                trimmedPhone.length < 7 -> Result.failure(Exception("El teléfono debe tener al menos 7 dígitos"))
                !isValidPhoneNumber(trimmedPhone) -> Result.failure(Exception("El formato del teléfono no es válido"))
                else -> contactRepository.createContact(trimmedName, trimmedPhone)
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
            val trimmedName = name.trim()
            val trimmedPhone = phone.trim()

            when {
                trimmedName.isEmpty() -> Result.failure(Exception("El nombre no puede estar vacío"))
                trimmedName.length < 2 -> Result.failure(Exception("El nombre debe tener al menos 2 caracteres"))
                trimmedPhone.isEmpty() -> Result.failure(Exception("El teléfono no puede estar vacío"))
                trimmedPhone.length < 7 -> Result.failure(Exception("El teléfono debe tener al menos 7 dígitos"))
                !isValidPhoneNumber(trimmedPhone) -> Result.failure(Exception("El formato del teléfono no es válido"))
                else -> contactRepository.updateContact(id, trimmedName, trimmedPhone)
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
            contactRepository.deleteContact(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sincroniza contactos con el servidor
     */
    suspend fun syncContacts(): Result<Unit> {
        return contactRepository.syncContacts()
    }

    /**
     * Obtiene contactos del teléfono
     */
    fun getPhoneContacts(): List<PhoneContact> {
        return contactRepository.getPhoneContacts()
    }

    /**
     * Observa el estado de la red
     */
    fun observeNetworkState(): Flow<Boolean> {
        return contactRepository.observeNetworkState()
    }

    /**
     * Obtiene estadísticas de sincronización
     */
    suspend fun getSyncStats(): Map<String, Int> {
        return contactRepository.getSyncStats()
    }

    /**
     * Limpia datos locales
     */
    suspend fun clearLocalData() {
        contactRepository.clearLocalData()
    }

    /**
     * Valida que el número de teléfono tenga un formato válido
     */
    private fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = Regex("^[+]?[0-9\\s\\-()]{7,20}$")
        return phoneRegex.matches(phone)
    }
}