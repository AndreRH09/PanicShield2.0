package com.example.panicshield.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.panicshield.domain.model.Contact

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "phone") val phone: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long = System.currentTimeMillis()
)

// Extensiones para convertir entre ContactEntity y Contact
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
        id = this.id ?: 0L,
        createdAt = this.createdAt,
        userId = this.userId,
        name = this.name,
        phone = this.phone
    )
}