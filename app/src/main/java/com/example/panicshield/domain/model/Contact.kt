package com.example.panicshield.domain.model

/**
 * Modelo de dominio para Contact
 */
data class Contact(
    val id: Long? = null,
    val createdAt: String? = null,
    val userId: String,
    val name: String,
    val phone: String
) {

    /**
     * Obtiene las iniciales del nombre del contacto
     */
    fun getInitials(): String {
        return name.split(" ")
            .take(2)
            .map { it.firstOrNull()?.uppercaseChar() ?: "" }
            .joinToString("")
            .ifEmpty { "?" }
    }

    /**
     * Formatea el número de teléfono para mostrar
     */
    fun getFormattedPhone(): String {
        // Ejemplo de formato simple: +51 999 888 777
        val cleanPhone = phone.replace(Regex("[^\\d+]"), "")
        return when {
            cleanPhone.startsWith("+51") && cleanPhone.length == 12 -> {
                "${cleanPhone.substring(0, 3)} ${cleanPhone.substring(3, 6)} ${cleanPhone.substring(6, 9)} ${cleanPhone.substring(9)}"
            }
            cleanPhone.length == 9 -> {
                "${cleanPhone.substring(0, 3)} ${cleanPhone.substring(3, 6)} ${cleanPhone.substring(6)}"
            }
            else -> phone
        }
    }
}

/**
 * Modelo para contactos del teléfono
 */
data class PhoneContact(
    val name: String,
    val phone: String
) {

    /**
     * Obtiene las iniciales del nombre del contacto
     */
    fun getInitials(): String {
        return name.split(" ")
            .take(2)
            .map { it.firstOrNull()?.uppercaseChar() ?: "" }
            .joinToString("")
            .ifEmpty { "?" }
    }
}