package com.lord_markus.ranobe_reader.settings.presentation

import androidx.lifecycle.ViewModel
import com.lord_markus.ranobe_reader.settings.domain.use_cases.SetDynamicColorUseCase
import com.lord_markus.ranobe_reader.settings.domain.use_cases.SetNightModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val setDynamicColorUseCase: SetDynamicColorUseCase,
    private val setNightModeUseCase: SetNightModeUseCase
) : ViewModel() {

    fun updateDynamicColor(on: Boolean) = runBlocking(IO) {
        setDynamicColorUseCase(on)
    }

    fun updateNightMode(flag: Boolean?) = runBlocking(IO) {
        setNightModeUseCase(flag)
    }
}
