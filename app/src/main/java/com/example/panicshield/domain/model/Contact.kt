package com.example.panicshield.domain.model

data class Contact(
    val id: Long? = null,
    val createdAt: String? = null,
    val userId: String,
    val name: String,
    val phone: String
)

// Para seleccionar contactos del teléfono
data class PhoneContact(
    val name: String,
    val phone: String
)