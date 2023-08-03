package com.lord_markus.ranobe_reader.auth

import androidx.compose.runtime.Composable
import com.lord_markus.ranobe_reader.auth.presentation.AuthScreen
import org.koin.compose.koinInject

data object Auth {
    @Composable
    fun Screen() = AuthScreen(viewModel = koinInject())
}
