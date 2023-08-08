package com.lord_markus.ranobe_reader.auth

import androidx.compose.runtime.Composable
import com.lord_markus.ranobe_reader.auth.presentation.AuthScreen
import com.lord_markus.ranobe_reader.auth.presentation.AuthViewModel
import com.lord_markus.ranobe_reader.core.models.UserState
import org.koin.androidx.compose.koinViewModel

data object Auth {
    @Composable
    fun Screen(onBackPressed: @Composable (() -> Unit) -> Unit, onSuccess: @Composable (UserState) -> Unit) =
        AuthScreen(
            getViewModel = { koinViewModel<AuthViewModel>() },
            onBackPressed = onBackPressed,
            onSuccess = onSuccess
        )
}
