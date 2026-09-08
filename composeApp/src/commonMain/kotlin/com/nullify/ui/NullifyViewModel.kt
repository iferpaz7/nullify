package com.nullify.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nullify.data.AllowedContact
import com.nullify.data.CallLogEntry
import com.nullify.data.repository.CallLogRepository
import com.nullify.data.repository.ContactRepository
import com.nullify.ui.state.UiState
import com.nullify.utils.EcuadorPhoneUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NullifyViewModel(
    private val contactRepository: ContactRepository,
    private val callLogRepository: CallLogRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val whitelist: StateFlow<UiState<List<AllowedContact>>> = combine(
        contactRepository.getAllAllowedContactsFlow(),
        _searchQuery
    ) { contacts, query ->
        if (query.isBlank()) {
            contacts
        } else {
            val q = query.trim().lowercase()
            contacts.filter {
                it.displayName.lowercase().contains(q) ||
                        it.normalizedNumber.contains(q)
            }
        }
    }
        .map<List<AllowedContact>, UiState<List<AllowedContact>>> { UiState.Success(it) }
        .catch { emit(UiState.Error("Error al cargar la lista blanca")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    val callLog: StateFlow<UiState<List<CallLogEntry>>> = callLogRepository
        .getRecentCalls()
        .map<List<CallLogEntry>, UiState<List<CallLogEntry>>> { UiState.Success(it) }
        .catch { emit(UiState.Error("Error al cargar el historial")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addManualContact(name: String, number: String) {
        viewModelScope.launch(ioDispatcher) {
            val normalized = EcuadorPhoneUtils.normalizeForDatabase(number)
            if (normalized.isNotEmpty()) {
                val newContact = AllowedContact(
                    normalizedNumber = normalized,
                    displayName = name.trim().ifEmpty { "Entidad Manual" }
                )
                contactRepository.insert(newContact)
            }
        }
    }

    fun removeContact(contact: AllowedContact) {
        viewModelScope.launch(ioDispatcher) {
            contactRepository.delete(contact)
        }
    }

    fun clearCallLog() {
        viewModelScope.launch(ioDispatcher) {
            callLogRepository.clearAll()
        }
    }
}
