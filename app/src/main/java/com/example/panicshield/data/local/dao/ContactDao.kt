package com.example.panicshield.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.example.panicshield.data.local.entity.ContactEntity

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY name ASC")
    fun getContactsByUserId(userId: String): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity): Long

    @Update
    suspend fun update(contact: ContactEntity)

    @Delete
    suspend fun delete(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: String)

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Long): ContactEntity?
}
