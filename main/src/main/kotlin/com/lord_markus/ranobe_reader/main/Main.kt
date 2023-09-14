package com.lord_markus.ranobe_reader.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.main.presentation.MainScreen

data object Main {
    @Composable
    fun Screen(
        modifier: Modifier,
        nightMode: Boolean?,
        updateNightMode: (Boolean?) -> Unit,
        dynamicMode: Boolean,
        updateDynamicMode: (Boolean) -> Unit,
        usersWithCurrentState: State<Pair<List<UserInfo>, Long?>>,
        addUser: (UserInfo, Boolean) -> Unit,
        removeUser: (List<UserInfo>) -> Unit,
        updateCurrent: (Long) -> Unit
    ) = MainScreen(
        modifier = modifier,
        nightMode = nightMode,
        dynamicMode = dynamicMode,
        updateDynamicMode = updateDynamicMode,
        updateNightMode = updateNightMode,
        usersWithCurrentState = usersWithCurrentState,
        addUser = addUser,
        removeUser = removeUser,
        updateCurrent = updateCurrent
    )
}
