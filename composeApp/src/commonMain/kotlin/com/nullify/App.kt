package com.nullify

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nullify.ui.NullifyViewModel
import com.nullify.ui.WhitelistScreen
import com.nullify.ui.theme.NullifyTheme

@Composable
fun NullifyApp(viewModel: NullifyViewModel) {
    NullifyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WhitelistScreen(viewModel = viewModel)
        }
    }
}
