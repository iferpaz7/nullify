package com.nullify

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.nullify.data.NullifyDatabase
import com.nullify.data.repository.CallLogRepository
import com.nullify.data.repository.ContactRepository
import com.nullify.data.repository.SettingsRepository
import com.nullify.di.appModule
import com.nullify.utils.AllowedNumberCache
import com.nullify.utils.ScreeningPolicyCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class NullifyApp : Application(), Configuration.Provider {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: NullifyDatabase
        private set

    val contactRepository: ContactRepository
        get() = get()

    val callLogRepository: CallLogRepository
        get() = get()

    val allowedNumberCache: AllowedNumberCache
        get() = get()

    val screeningPolicyCache: ScreeningPolicyCache
        get() = get()

    val settingsRepository: SettingsRepository
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
                    single { database.settingsDao() }
                },
                appModule
            )
        }

        prewarmDatabase()
        mirrorWhitelistToCache()
        mirrorScreeningPolicyToCache()
    }

    /**
     * Keeps the in-memory screening cache fed from Room so
     * CallScreeningService never pays DB latency on the hot path.
     */
    private fun mirrorWhitelistToCache() {
        appScope.launch {
            try {
                val cache: AllowedNumberCache = get()
                contactRepository.getAllAllowedContactsFlow()
                    .catch { Log.e("NullifyApp", "Whitelist flow failed", it) }
                    .collect { contacts ->
                        cache.replaceAll(contacts.map { it.normalizedNumber })
                    }
            } catch (e: Exception) {
                Log.e("NullifyApp", "Failed to mirror whitelist to cache", e)
            }
        }
    }

    /**
     * Keeps the screening master switch mirrored into memory so the service
     * never has to touch Room on the hot path after the first emission.
     */
    private fun mirrorScreeningPolicyToCache() {
        appScope.launch {
            try {
                val cache: ScreeningPolicyCache = get()
                val repository: SettingsRepository = get()
                repository.getSettingsFlow()
                    .catch { Log.e("NullifyApp", "Screening policy flow failed", it) }
                    .collect { settings ->
                        cache.update(settings.screeningEnabled)
                    }
            } catch (e: Exception) {
                Log.e("NullifyApp", "Failed to mirror screening policy to cache", e)
            }
        }
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
