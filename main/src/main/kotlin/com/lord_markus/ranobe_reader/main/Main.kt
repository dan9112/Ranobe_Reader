package com.lord_markus.ranobe_reader.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.main.presentation.MainScreen

data object Main {
    @Composable
    fun Screen(
        modifier: Modifier,
        users: List<UserInfo>,
        updateSignedIn: (List<UserInfo>) -> Unit
    ) = MainScreen(
        modifier = modifier,
        users = users,
        updateSignedIn = updateSignedIn
    )
}
