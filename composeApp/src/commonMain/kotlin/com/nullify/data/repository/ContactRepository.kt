package com.nullify.data.repository

import com.nullify.data.AllowedContact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun isNumberAllowed(number: String): Boolean
    fun getAllAllowedContactsFlow(): Flow<List<AllowedContact>>
    suspend fun insert(contact: AllowedContact)
    fun insertAll(contacts: List<AllowedContact>)
    suspend fun delete(contact: AllowedContact)
    fun clearAll()
}
