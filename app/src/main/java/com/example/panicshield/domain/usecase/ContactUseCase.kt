package com.example.panicshield.domain.usecase

import com.example.panicshield.data.repository.ContactRepository
import com.example.panicshield.domain.model.Contact
import com.example.panicshield.domain.model.PhoneContact
import com.example.panicshield.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContactUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {

    suspend fun getContacts(): Flow<Resource<List<Contact>>> {
        return contactRepository.getContacts()
    }

    suspend fun syncContacts(): Flow<Resource<List<Contact>>> {
        return contactRepository.syncContacts()
    }

    suspend fun createContact(name: String, phone: String): Result<Contact> {
        if (name.isBlank()) {
            return Result.failure(Exception("El nombre no puede estar vacío"))
        }

        if (phone.isBlank()) {
            return Result.failure(Exception("El teléfono no puede estar vacío"))
        }

        val cleanPhone = phone.replace("[^+\\d]".toRegex(), "")
        if (cleanPhone.length < 7) {
            return Result.failure(Exception("El teléfono debe tener al menos 7 dígitos"))
        }
        
        return contactRepository.createContact(name.trim(), cleanPhone)
    }
    
    suspend fun updateContact(id: Long, name: String, phone: String): Result<Contact> {
        if (name.isBlank()) {
            return Result.failure(Exception("El nombre no puede estar vacío"))
        }
        
        if (phone.isBlank()) {
            return Result.failure(Exception("El teléfono no puede estar vacío"))
        }
        
        val cleanPhone = phone.replace("[^+\\d]".toRegex(), "")
        if (cleanPhone.length < 7) {
            return Result.failure(Exception("El teléfono debe tener al menos 7 dígitos"))
        }
        
        return contactRepository.updateContact(id, name.trim(), cleanPhone)
    }
    
    suspend fun deleteContact(id: Long): Result<Unit> {
        return contactRepository.deleteContact(id)
    }
    
    fun getPhoneContacts(): List<PhoneContact> {
        return contactRepository.getPhoneContacts()
    }
}