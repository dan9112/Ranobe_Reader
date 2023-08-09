package com.lord_markus.ranobe_reader.main

import androidx.compose.runtime.Composable
import com.lord_markus.ranobe_reader.main.presentation.MainScreen

data object Main {
    @Composable
    fun Screen(onBackPressed: @Composable (() -> Unit) -> Unit, goOut: () -> Unit) =
        MainScreen(
            onBackPressed = onBackPressed,
            goOut = goOut
        )
}
