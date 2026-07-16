package com.nullify.data.repository

import com.nullify.data.CallLogDao
import com.nullify.data.CallLogEntry
import kotlinx.coroutines.flow.Flow

class CallLogRepositoryImpl(
    private val callLogDao: CallLogDao,
) : CallLogRepository {

    override suspend fun insert(entry: CallLogEntry) =
        callLogDao.insert(entry)

    override fun getRecentCalls(): Flow<List<CallLogEntry>> =
        callLogDao.getRecentCalls()

    override suspend fun clearAll() =
        callLogDao.clearAll()
}
