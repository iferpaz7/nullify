package com.nullify.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    /** Null until the seed row exists, so the repository can apply defaults. */
    @Query("SELECT * FROM screening_settings WHERE id = 0")
    fun getFlow(): Flow<ScreeningSettings?>

    @Query("SELECT * FROM screening_settings WHERE id = 0")
    suspend fun getOnce(): ScreeningSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: ScreeningSettings)
}
