package com.nullify.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Singleton row (id = 0) holding the user's screening master switch.
 *
 * When [screeningEnabled] is false the CallScreeningService allows every
 * incoming call; when true (the default) unknown numbers are blocked.
 */
@Entity(tableName = "screening_settings")
data class ScreeningSettings(
    @PrimaryKey val id: Int = 0,
    val screeningEnabled: Boolean = true,
)
