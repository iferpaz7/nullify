package com.nullify

import androidx.compose.ui.window.ComposeUIViewController
import com.nullify.data.NullifyDatabase
import com.nullify.ui.NullifyViewModel

fun MainViewController() = ComposeUIViewController {
    val db = createNullifyDatabase()
    val viewModel = NullifyViewModel(db.contactDao())
    NullifyApp(viewModel = viewModel)
}
