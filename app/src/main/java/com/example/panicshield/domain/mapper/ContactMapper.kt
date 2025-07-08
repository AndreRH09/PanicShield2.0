package com.example.panicshield.domain.mapper

import com.example.panicshield.data.local.entity.ContactEntity
import com.example.panicshield.data.remote.dto.ContactRemoteDto
import com.example.panicshield.domain.model.Contact
import com.example.supabaseofflinesupport.OfflineCrudType

object ContactMapper {
    
    // Domain to Entity
    fun Contact.toEntity(offlineOpType: Int = OfflineCrudType.NONE.ordinal): ContactEntity {
        return ContactEntity(
            id = this.id?.toInt() ?: 0,
            lastUpdatedTimestamp = System.currentTimeMillis(),
            offlineFieldOpType = offlineOpType,
            userId = this.userId,
            name = this.name,
            phone = this.phone,
            createdAt = this.createdAt
        )
    }
    
    // Entity to Domain
    fun ContactEntity.toDomain(): Contact {
        return Contact(
            id = this.id.toLong(),
            createdAt = this.createdAt,
            userId = this.userId,
            name = this.name,
            phone = this.phone
        )
    }
    
    // RemoteDto to Entity
    fun ContactRemoteDto.toEntity(): ContactEntity {
        return ContactEntity(
            id = this.id,
            lastUpdatedTimestamp = this.lastUpdatedTimestamp,
            offlineFieldOpType = OfflineCrudType.NONE.ordinal,
            userId = this.userId,
            name = this.name,
            phone = this.phone,
            createdAt = this.createdAt
        )
    }
    
    // Entity to RemoteDto
    fun ContactEntity.toRemoteDto(): ContactRemoteDto {
        return ContactRemoteDto(
            id = this.id,
            lastUpdatedTimestamp = this.lastUpdatedTimestamp,
            userId = this.userId,
            name = this.name,
            phone = this.phone,
            createdAt = this.createdAt
        )
    }
    
    // Domain to RemoteDto
    fun Contact.toRemoteDto(): ContactRemoteDto {
        return ContactRemoteDto(
            id = this.id?.toInt() ?: 0,
            lastUpdatedTimestamp = System.currentTimeMillis(),
            userId = this.userId,
            name = this.name,
            phone = this.phone,
            createdAt = this.createdAt
        )
    }
    
    // RemoteDto to Domain
    fun ContactRemoteDto.toDomain(): Contact {
        return Contact(
            id = this.id.toLong(),
            createdAt = this.createdAt,
            userId = this.userId,
            name = this.name,
            phone = this.phone
        )
    }
}