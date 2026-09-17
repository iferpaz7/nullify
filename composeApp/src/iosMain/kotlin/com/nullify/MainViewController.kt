package com.nullify

import androidx.compose.ui.window.ComposeUIViewController
import com.nullify.data.NullifyDatabase
import com.nullify.data.repository.CallLogRepositoryImpl
import com.nullify.data.repository.ContactRepositoryImpl
import com.nullify.ui.NullifyViewModel
import com.nullify.utils.AllowedNumberCache

fun MainViewController() = ComposeUIViewController {
    val db = createNullifyDatabase()
    val viewModel = NullifyViewModel(
        contactRepository = ContactRepositoryImpl(db.contactDao()),
        callLogRepository = CallLogRepositoryImpl(db.callLogDao()),
        allowedNumberCache = AllowedNumberCache(),
    )
    NullifyApp(viewModel = viewModel)
}
