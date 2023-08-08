package com.lord_markus.ranobe_reader.main

import androidx.compose.runtime.Composable
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.main.presentation.MainScreen

data object Main {
    @Composable
    fun Screen(onBackPressed: @Composable (() -> Unit) -> Unit, onSuccess: @Composable (UserInfo) -> Unit) =
        MainScreen(
            onBackPressed = onBackPressed,
            onSuccess = onSuccess
        )
}
