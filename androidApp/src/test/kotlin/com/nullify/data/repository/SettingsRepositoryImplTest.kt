package com.nullify.data.repository

import com.nullify.data.ScreeningSettings
import com.nullify.data.SettingsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryImplTest {

    @Test
    fun testMissingRow_defaultsToScreeningEnabled() = runTest {
        val repository = SettingsRepositoryImpl(FakeSettingsDao())

        assertTrue(repository.isScreeningEnabled())
        assertEquals(
            ScreeningSettings(screeningEnabled = true),
            repository.getSettingsFlow().first(),
        )
    }

    @Test
    fun testSetScreeningEnabledDisablesScreening() = runTest {
        val repository = SettingsRepositoryImpl(FakeSettingsDao())

        repository.setScreeningEnabled(false)
        assertFalse(repository.isScreeningEnabled())
        assertEquals(
            ScreeningSettings(screeningEnabled = false),
            repository.getSettingsFlow().first(),
        )
    }

    @Test
    fun testSetScreeningEnabledRoundTripsThroughFlow() = runTest {
        val repository = SettingsRepositoryImpl(FakeSettingsDao())

        repository.setScreeningEnabled(false)
        repository.setScreeningEnabled(true)

        assertTrue(repository.isScreeningEnabled())
        assertEquals(
            ScreeningSettings(screeningEnabled = true),
            repository.getSettingsFlow().first(),
        )
    }
}

private class FakeSettingsDao : SettingsDao {
    private val state = MutableStateFlow<ScreeningSettings?>(null)

    override fun getFlow(): Flow<ScreeningSettings?> = state

    override suspend fun getOnce(): ScreeningSettings? = state.value

    override suspend fun upsert(settings: ScreeningSettings) {
        state.value = settings
    }
}
