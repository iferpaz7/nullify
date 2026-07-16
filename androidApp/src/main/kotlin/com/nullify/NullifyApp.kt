package com.nullify

import android.app.Application
import androidx.work.Configuration
import com.nullify.data.NullifyDatabase
import com.nullify.data.repository.CallLogRepository
import com.nullify.data.repository.CallLogRepositoryImpl
import com.nullify.data.repository.ContactRepository
import com.nullify.data.repository.ContactRepositoryImpl

class NullifyApp : Application(), Configuration.Provider {

    lateinit var database: NullifyDatabase
        private set

    lateinit var contactRepository: ContactRepository
        private set

    lateinit var callLogRepository: CallLogRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = createNullifyDatabase(this)
        contactRepository = ContactRepositoryImpl(database.contactDao())
        callLogRepository = CallLogRepositoryImpl(database.callLogDao())
        prewarmDatabase()
    }

    private fun prewarmDatabase() {
        Thread {
            try {
                database.openHelper.writableDatabase
            } catch (_: Exception) {
            }
        }.start()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
