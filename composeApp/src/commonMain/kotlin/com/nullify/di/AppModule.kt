package com.nullify.di

import com.nullify.data.repository.CallLogRepository
import com.nullify.data.repository.CallLogRepositoryImpl
import com.nullify.data.repository.ContactRepository
import com.nullify.data.repository.ContactRepositoryImpl
import com.nullify.ui.NullifyViewModel
import org.koin.dsl.module

val appModule = module {
    single<ContactRepository> { ContactRepositoryImpl(get()) }
    single<CallLogRepository> { CallLogRepositoryImpl(get()) }
    factory { NullifyViewModel(get(), get()) }
}
