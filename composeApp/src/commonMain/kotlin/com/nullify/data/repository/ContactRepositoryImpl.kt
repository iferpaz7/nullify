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

    override suspend fun insert(contact: AllowedContact) =
        contactDao.insert(contact)

    override fun insertAll(contacts: List<AllowedContact>) =
        contactDao.insertAll(contacts)

    override suspend fun delete(contact: AllowedContact) =
        contactDao.delete(contact)

    override fun clearAll() =
        contactDao.clearAll()
}
