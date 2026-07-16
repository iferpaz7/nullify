package com.nullify

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.nullify.data.NullifyDatabase

class NullifyApp : Application(), Configuration.Provider {

    lateinit var database: NullifyDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = createNullifyDatabase(this)
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
