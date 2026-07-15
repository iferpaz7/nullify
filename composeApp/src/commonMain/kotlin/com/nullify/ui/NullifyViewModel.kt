package com.nullify.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nullify.data.AllowedContact
import com.nullify.data.ContactDao
import com.nullify.utils.EcuadorPhoneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NullifyViewModel(private val contactDao: ContactDao) : ViewModel() {

    val whitelist: StateFlow<List<AllowedContact>> = contactDao.getAllAllowedContactsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addManualContact(name: String, number: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val normalized = EcuadorPhoneUtils.normalizeForDatabase(number)
            if (normalized.isNotEmpty()) {
                val newContact = AllowedContact(
                    normalizedNumber = normalized,
                    displayName = name.trim().ifEmpty { "Entidad Manual" }
                )
                contactDao.insert(newContact)
            }
        }
    }

    fun removeContact(contact: AllowedContact) {
        viewModelScope.launch(Dispatchers.IO) {
            contactDao.delete(contact)
        }
    }
}

class NullifyViewModelFactory(private val contactDao: ContactDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NullifyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NullifyViewModel(contactDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
