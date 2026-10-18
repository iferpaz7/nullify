package com.nullify.di

import com.nullify.data.repository.CallLogRepository
import com.nullify.data.repository.CallLogRepositoryImpl
import com.nullify.data.repository.ContactRepository
import com.nullify.data.repository.ContactRepositoryImpl
import com.nullify.data.repository.SettingsRepository
import com.nullify.data.repository.SettingsRepositoryImpl
import com.nullify.ui.NullifyViewModel
import com.nullify.utils.AllowedNumberCache
import com.nullify.utils.ScreeningPolicyCache
import org.koin.dsl.module

val appModule = module {
    single { AllowedNumberCache() }
    single { ScreeningPolicyCache() }
    single<ContactRepository> { ContactRepositoryImpl(get()) }
    single<CallLogRepository> { CallLogRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    factory { NullifyViewModel(get(), get(), get(), get(), get()) }
}
