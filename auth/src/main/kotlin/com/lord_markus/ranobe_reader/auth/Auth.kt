package com.lord_markus.ranobe_reader.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lord_markus.ranobe_reader.auth.presentation.AuthScreen
import com.lord_markus.ranobe_reader.core.models.UserInfo

data object Auth {
    @Composable
    fun Screen(
        modifier: Modifier,
        onBackPressed: @Composable (() -> Unit) -> Unit,
        onSuccess: (List<UserInfo>, Long) -> Unit
    ) = AuthScreen(
        modifier = modifier,
        onBackPressed = onBackPressed,
        onSuccess = onSuccess
    )
}
