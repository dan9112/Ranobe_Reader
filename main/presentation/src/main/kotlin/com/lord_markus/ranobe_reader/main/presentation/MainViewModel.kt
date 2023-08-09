package com.lord_markus.ranobe_reader.main.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lord_markus.ranobe_reader.main.domain.models.SignOutResultMain
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
//    private val setCurrentUseCase: SetCurrentUseCase
) : ViewModel() {
    val signedIn = savedStateHandler.getStateFlow<ExtendedMainUseCaseState<SignOutResultMain>>(
        KEY, ExtendedMainUseCaseState.Default
    )

    fun signOut(withRemove: Boolean = false) {
        viewModelScope.launch {
            savedStateHandler[KEY] = MainUseCaseState.InProcess
            savedStateHandler[KEY] = MainUseCaseState.ResultReceived(
                result = if (withRemove) signOutWithRemoveUseCase() else signOutUseCase()
            )
        }
    }

    fun caughtTrigger() {
        val state = signedIn.value
        if (state is MainUseCaseState.ResultReceived) savedStateHandler[KEY] = state.copy(trigger = false)
    }

    fun resetSignedIn() {
        savedStateHandler[KEY] = ExtendedMainUseCaseState.Default
    }

    private companion object {
        private const val KEY = "key"
    }
}
