package com.nullify.data.repository

import com.nullify.data.CallLogEntry
import kotlinx.coroutines.flow.Flow

interface CallLogRepository {
    suspend fun insert(entry: CallLogEntry)
    fun getRecentCalls(): Flow<List<CallLogEntry>>
    suspend fun clearAll()
}
