package com.nullify.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {

    @Insert
    suspend fun insert(entry: CallLogEntry)

    @Query("SELECT * FROM call_log ORDER BY timestamp DESC LIMIT 200")
    fun getRecentCalls(): Flow<List<CallLogEntry>>

    @Query("DELETE FROM call_log")
    suspend fun clearAll()
}
