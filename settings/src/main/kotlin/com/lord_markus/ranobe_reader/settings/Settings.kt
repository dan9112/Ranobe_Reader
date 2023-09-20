package com.lord_markus.ranobe_reader.settings

import androidx.compose.runtime.Composable
import com.lord_markus.ranobe_reader.settings.presentation.SettingsScreen

object Settings {
    @Composable
    fun Screen(nightMode: Boolean?, dynamicMode: Boolean) = SettingsScreen(nightMode, dynamicMode)
}