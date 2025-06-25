package com.example.panicshield.data.mapper

import com.example.panicshield.data.local.entity.ContactEntity
import com.example.panicshield.data.remote.dto.ContactDto
import com.example.panicshield.domain.model.Contact

fun ContactDto.toEntity(userId: String): ContactEntity {
    return ContactEntity(
        id = this.id,
        createdAt = this.createdAt,
        userId = userId,
        name = this.name,
        phone = this.phone,
        lastFetchedTime = System.currentTimeMillis()
    )
}

fun ContactEntity.toDomain(): Contact {
    return Contact(
        id = this.id,
        createdAt = this.createdAt,
        userId = this.userId,
        name = this.name,
        phone = this.phone
    )
}

fun Contact.toEntity(): ContactEntity {
    return ContactEntity(
        id = this.id,
        createdAt = this.createdAt,
        userId = this.userId,
        name = this.name,
        phone = this.phone
    )
}

fun List<ContactDto>.toEntityList(userId: String): List<ContactEntity> {
    return this.map { it.toEntity(userId) }
}

fun List<ContactEntity>.toDomainList(): List<Contact> {
    return this.map { it.toDomain() }
}
