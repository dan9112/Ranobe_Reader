package com.lord_markus.ranobe_reader.auth

import androidx.compose.runtime.Composable
import com.lord_markus.ranobe_reader.auth.presentation.AuthScreen
import com.lord_markus.ranobe_reader.auth.presentation.AuthViewModel
import com.lord_markus.ranobe_reader.core.UserInfo
import org.koin.androidx.compose.koinViewModel

data object Auth {
    @Composable
    fun Screen(onBack: @Composable ((() -> Unit)?) -> Unit, onSuccess: @Composable (UserInfo) -> Unit) =
        AuthScreen(getViewModel = { koinViewModel<AuthViewModel>() }, onBack = onBack, onSuccess = onSuccess)
}
