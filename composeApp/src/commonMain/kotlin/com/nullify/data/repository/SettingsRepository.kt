package com.nullify.data.repository

import com.nullify.data.ScreeningSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettingsFlow(): Flow<ScreeningSettings>
    suspend fun isScreeningEnabled(): Boolean
    suspend fun setScreeningEnabled(enabled: Boolean)
}
