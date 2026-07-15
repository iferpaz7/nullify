package com.nullify.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT EXISTS(SELECT * FROM allowed_contacts WHERE normalizedNumber = :number)")
    fun isNumberAllowed(number: String): Boolean

    @Query("SELECT * FROM allowed_contacts ORDER BY displayName ASC")
    fun getAllAllowedContactsFlow(): Flow<List<AllowedContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: AllowedContact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(contacts: List<AllowedContact>)

    @Delete
    suspend fun delete(contact: AllowedContact)

    @Query("DELETE FROM allowed_contacts")
    fun clearAll()
}
