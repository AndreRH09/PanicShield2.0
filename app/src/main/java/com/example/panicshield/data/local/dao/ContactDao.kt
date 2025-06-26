package com.example.panicshield.data.local.dao

import androidx.room.*
import com.example.panicshield.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts WHERE user_id = :userId ORDER BY name ASC")
    fun getContactsByUserId(userId: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Long): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("DELETE FROM contacts WHERE user_id = :userId")
    suspend fun deleteAllContactsByUserId(userId: String)

    @Query("SELECT COUNT(*) FROM contacts WHERE user_id = :userId")
    suspend fun getContactsCount(userId: String): Int

    @Query("SELECT MAX(last_updated) FROM contacts WHERE user_id = :userId")
    suspend fun getLastUpdateTime(userId: String): Long?
}