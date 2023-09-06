package com.lord_markus.ranobe_reader.main.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lord_markus.ranobe_reader.auth_core.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth_core.domain.use_cases.SignUpUseCase
import com.lord_markus.ranobe_reader.auth_core.presentation.AuthCoreViewModel
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthScreenState
import com.lord_markus.ranobe_reader.auth_core.presentation.models.ExtendedAuthUseCaseState
import com.lord_markus.ranobe_reader.main.domain.models.SetCurrentResultMain
import com.lord_markus.ranobe_reader.main.domain.models.SignOutResultMain
import com.lord_markus.ranobe_reader.main.domain.use_cases.SetCurrentUseCase
import com.lord_markus.ranobe_reader.main.domain.use_cases.SignOutUseCase
import com.lord_markus.ranobe_reader.main.domain.use_cases.SignOutWithRemoveUseCase
import com.lord_markus.ranobe_reader.main.presentation.models.MainUseCaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandler: SavedStateHandle,
    private val signOutUseCase: SignOutUseCase,
    private val signOutWithRemoveUseCase: SignOutWithRemoveUseCase,
    private val setCurrentUseCase: SetCurrentUseCase,
    signInUseCase: SignInUseCase,
    signUpUseCase: SignUpUseCase
) : AuthCoreViewModel(savedStateHandler, signInUseCase, signUpUseCase) {
    val signOutFlow = savedStateHandler.getStateFlow<MainUseCaseState<SignOutResultMain>>(
        LIST_KEY, MainUseCaseState.InProcess
    )
    val setCurrentFlow = savedStateHandler.getStateFlow<MainUseCaseState<SetCurrentResultMain>>(
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
        val currentState = signOutFlow.value
        if (currentState is MainUseCaseState.ResultReceived && currentState.result is SignOutResultMain.Error)
            savedStateHandler[LIST_KEY] = MainUseCaseState.ResultReceived(currentState.result.copy(trigger = false))
    }

    val dialogInUse = savedStateHandler.getStateFlow(DIALOG_KEY, false)

    fun switchDialog(newValue: Boolean) {
        savedStateHandler[DIALOG_KEY] = newValue
    }

    val progressBarVisible = savedStateHandler.getStateFlow(AUTH_CORE_PROGRESS_BAR_KEY, false)

    fun switchProgressBar(newValue: Boolean) {
        savedStateHandler[AUTH_CORE_PROGRESS_BAR_KEY] = newValue
    }

    fun resetAuthCoreViewModel() {
        savedStateHandler[AUTH_SCREEN_STATE_KEY] = AuthScreenState.SignIn
        savedStateHandler[SIGN_IN_STATE_KEY] = ExtendedAuthUseCaseState.Default
        savedStateHandler[SIGN_UP_STATE_KEY] = ExtendedAuthUseCaseState.Default
        savedStateHandler[AUTH_CORE_PROGRESS_BAR_KEY] = false
    }

    private companion object {
        const val LIST_KEY = "list_key"
        const val CURRENT_KEY = "current_key"
        const val AUTH_CORE_PROGRESS_BAR_KEY = "progress_bar"
        const val DIALOG_KEY = "dialog"
    }
}
