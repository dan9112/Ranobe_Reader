package com.lord_markus.ranobe_reader.main.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lord_markus.ranobe_reader.main.domain.models.SetCurrentResultMain
import com.lord_markus.ranobe_reader.main.domain.models.SignOutResultMain
import com.lord_markus.ranobe_reader.main.domain.use_cases.SetCurrentUseCase
import com.lord_markus.ranobe_reader.main.domain.use_cases.SignOutUseCase
import com.lord_markus.ranobe_reader.main.domain.use_cases.SignOutWithRemoveUseCase
import com.lord_markus.ranobe_reader.main.presentation.models.ExtendedMainUseCaseState
import com.lord_markus.ranobe_reader.main.presentation.models.MainUseCaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandler: SavedStateHandle,
    private val signOutUseCase: SignOutUseCase,
    private val signOutWithRemoveUseCase: SignOutWithRemoveUseCase,
    private val setCurrentUseCase: SetCurrentUseCase
) : ViewModel() {
    val signedIn = savedStateHandler.getStateFlow<ExtendedMainUseCaseState<SignOutResultMain>>(
        LIST_KEY, ExtendedMainUseCaseState.Default
    )
    val current = savedStateHandler.getStateFlow<MainUseCaseState<SetCurrentResultMain>>(
        CURRENT_KEY, MainUseCaseState.InProcess
    )

    fun signOut(withRemove: Boolean = false) {
        viewModelScope.launch {
            savedStateHandler[LIST_KEY] = MainUseCaseState.InProcess
            savedStateHandler[LIST_KEY] = MainUseCaseState.ResultReceived(
                result = if (withRemove) signOutWithRemoveUseCase() else signOutUseCase()
            )
        }
    }

    fun setCurrent(id: Long) {
        viewModelScope.launch {
            savedStateHandler[CURRENT_KEY] = MainUseCaseState.InProcess
            savedStateHandler[CURRENT_KEY] = MainUseCaseState.ResultReceived(
                result = setCurrentUseCase(id)
            )
        }
    }

    fun caughtTrigger() {
        val state = signedIn.value
        if (state is MainUseCaseState.ResultReceived) savedStateHandler[LIST_KEY] = state.copy(trigger = false)
    }

    private companion object {
        const val LIST_KEY = "list_key"
        const val CURRENT_KEY = "current_key"
    }
}
