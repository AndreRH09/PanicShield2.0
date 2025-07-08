package com.example.panicshield.data.local.dao

import androidx.room.*
import com.example.panicshield.data.local.entity.ContactEntity
import com.example.supabaseofflinesupport.GenericDao
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao : GenericDao<ContactEntity> {

    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY name ASC")
    fun getContactsByUserId(userId: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY name ASC")
    suspend fun getContactsByUserIdSync(userId: String): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Int): ContactEntity?

    @Query("DELETE FROM contacts WHERE userId = :userId")
    suspend fun deleteAllContactsByUserId(userId: String)

    @Query("UPDATE contacts SET offlineFieldOpType = :opType, lastUpdatedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateOfflineOpType(id: Int, opType: Int, timestamp: Long)
}       