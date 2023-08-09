package com.lord_markus.ranobe_reader.auth

import androidx.compose.runtime.Composable
import com.lord_markus.ranobe_reader.auth.presentation.AuthScreen
import com.lord_markus.ranobe_reader.core.models.UserState

data object Auth {
    @Composable
    fun Screen(onBackPressed: @Composable (() -> Unit) -> Unit, onSuccess: (UserState) -> Unit) =
        AuthScreen(
            onBackPressed = onBackPressed,
            onSuccess = onSuccess
        )
}
