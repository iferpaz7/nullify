package com.nullify.data.repository

import com.nullify.data.ScreeningSettings
import com.nullify.data.SettingsDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao,
) : SettingsRepository {

    override fun getSettingsFlow(): Flow<ScreeningSettings> =
        settingsDao.getFlow().map { it ?: ScreeningSettings() }

    override suspend fun isScreeningEnabled(): Boolean =
        settingsDao.getOnce()?.screeningEnabled ?: ScreeningSettings().screeningEnabled

    override suspend fun setScreeningEnabled(enabled: Boolean) =
        settingsDao.upsert(ScreeningSettings(screeningEnabled = enabled))
}
