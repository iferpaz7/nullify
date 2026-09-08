package com.nullify.ui

import com.nullify.data.AllowedContact
import com.nullify.data.CallLogEntry
import com.nullify.data.repository.CallLogRepository
import com.nullify.data.repository.ContactRepository
import com.nullify.ui.state.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NullifyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var contactRepository: FakeContactRepository
    private lateinit var callLogRepository: FakeCallLogRepository
    private lateinit var viewModel: NullifyViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        contactRepository = FakeContactRepository()
        callLogRepository = FakeCallLogRepository()
        viewModel = NullifyViewModel(
            contactRepository = contactRepository,
            callLogRepository = callLogRepository,
            ioDispatcher = testDispatcher,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testAddManualContact_normalizesNumberAndInserts() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.whitelist.collect {}
        }

        viewModel.addManualContact("BGR Bank", "023965006")
        advanceUntilIdle()

        val state = viewModel.whitelist.value
        assertTrue(state is UiState.Success)
        val list = (state as UiState.Success).data
        assertEquals(1, list.size)
        assertEquals("23965006", list[0].normalizedNumber)
        assertEquals("BGR Bank", list[0].displayName)
    }

    @Test
    fun testSearchQuery_filtersWhitelist() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.whitelist.collect {}
        }

        contactRepository.insert(AllowedContact("23965006", "BGR Bank"))
        contactRepository.insert(AllowedContact("991234567", "John Doe"))
        advanceUntilIdle()

        viewModel.setSearchQuery("John")
        advanceUntilIdle()

        val state = viewModel.whitelist.value
        assertTrue(state is UiState.Success)
        val list = (state as UiState.Success).data
        assertEquals(1, list.size)
        assertEquals("John Doe", list[0].displayName)
    }

    @Test
    fun testRemoveContact_removesFromWhitelist() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.whitelist.collect {}
        }

        val contact = AllowedContact("23965006", "BGR Bank")
        contactRepository.insert(contact)
        advanceUntilIdle()

        viewModel.removeContact(contact)
        advanceUntilIdle()

        val state = viewModel.whitelist.value
        assertTrue(state is UiState.Success)
        val list = (state as UiState.Success).data
        assertTrue(list.isEmpty())
    }

    @Test
    fun testClearCallLog_clearsAllEntries() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.callLog.collect {}
        }

        callLogRepository.insert(
            CallLogEntry(
                phoneNumber = "0991234567",
                result = "BLOCKED",
                reason = "unknown number",
                timestamp = System.currentTimeMillis(),
            )
        )
        advanceUntilIdle()

        viewModel.clearCallLog()
        advanceUntilIdle()

        val state = viewModel.callLog.value
        assertTrue(state is UiState.Success)
        val list = (state as UiState.Success).data
        assertTrue(list.isEmpty())
    }
}

private class FakeContactRepository : ContactRepository {
    private val contactsFlow = MutableStateFlow<List<AllowedContact>>(emptyList())

    override fun isNumberAllowed(number: String): Boolean {
        return contactsFlow.value.any { it.normalizedNumber == number }
    }

    override fun getAllAllowedContactsFlow(): Flow<List<AllowedContact>> = contactsFlow

    override suspend fun insert(contact: AllowedContact) {
        contactsFlow.value = contactsFlow.value.filter { it.normalizedNumber != contact.normalizedNumber } + contact
    }

    override suspend fun insertAll(contacts: List<AllowedContact>) {
        contactsFlow.value = contacts
    }

    override suspend fun delete(contact: AllowedContact) {
        contactsFlow.value = contactsFlow.value.filter { it.normalizedNumber != contact.normalizedNumber }
    }

    override suspend fun clearAll() {
        contactsFlow.value = emptyList()
    }
}

private class FakeCallLogRepository : CallLogRepository {
    private val callLogFlow = MutableStateFlow<List<CallLogEntry>>(emptyList())

    override suspend fun insert(entry: CallLogEntry) {
        callLogFlow.value = listOf(entry) + callLogFlow.value
    }

    override fun getRecentCalls(): Flow<List<CallLogEntry>> = callLogFlow

    override suspend fun clearAll() {
        callLogFlow.value = emptyList()
    }
}
