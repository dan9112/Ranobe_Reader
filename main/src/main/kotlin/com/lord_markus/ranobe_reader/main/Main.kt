package com.lord_markus.ranobe_reader.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.main.presentation.MainScreen

data object Main {
    @Composable
    fun Screen(
        modifier: Modifier,
        onBackPressed: @Composable (() -> Unit) -> Unit,
        users: List<UserInfo>,
        currentId: Long,
        updateSignedIn: (List<UserInfo>, Long?) -> Unit
    ) = MainScreen(
        modifier = modifier,
        onBackPressed = onBackPressed,
        users = users,
        currentId = currentId,
        updateSignedIn = updateSignedIn
    )
}
