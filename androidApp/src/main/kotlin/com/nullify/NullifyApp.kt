package com.nullify

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.nullify.data.NullifyDatabase
import com.nullify.data.repository.CallLogRepository
import com.nullify.data.repository.ContactRepository
import com.nullify.di.appModule
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class NullifyApp : Application(), Configuration.Provider {

    lateinit var database: NullifyDatabase
        private set

    val contactRepository: ContactRepository
        get() = get()

    val callLogRepository: CallLogRepository
        get() = get()

    override fun onCreate() {
        super.onCreate()
        database = createNullifyDatabase(this)

        startKoin {
            androidLogger()
            androidContext(this@NullifyApp)
            modules(
                module {
                    single { database }
                    single { database.contactDao() }
                    single { database.callLogDao() }
                },
                appModule
            )
        }

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
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}
