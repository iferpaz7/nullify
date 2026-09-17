package com.nullify.data.repository

import com.nullify.data.AllowedContact
import com.nullify.data.ContactDao
import kotlinx.coroutines.flow.Flow

class ContactRepositoryImpl(
    private val contactDao: ContactDao,
) : ContactRepository {

    override fun isNumberAllowed(number: String): Boolean =
        contactDao.isNumberAllowed(number)

    override fun getAllAllowedContactsFlow(): Flow<List<AllowedContact>> =
        contactDao.getAllAllowedContactsFlow()

    override suspend fun getAllOnce(): List<AllowedContact> =
        contactDao.getAllOnce()

    override suspend fun insert(contact: AllowedContact) =
        contactDao.insert(contact)

    override suspend fun insertAll(contacts: List<AllowedContact>) =
        contactDao.insertAll(contacts)

    override suspend fun delete(contact: AllowedContact) =
        contactDao.delete(contact)

    override suspend fun deleteByNumbers(numbers: List<String>) {
        if (numbers.isNotEmpty()) contactDao.deleteByNumbers(numbers)
    }

    override suspend fun clearAll() =
        contactDao.clearAll()
}
