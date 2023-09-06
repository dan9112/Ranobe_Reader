package com.lord_markus.ranobe_reader.auth.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth.domain.use_cases.GetSignedInUsersUseCase
import com.lord_markus.ranobe_reader.auth_core.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth_core.domain.use_cases.SignUpUseCase
import com.lord_markus.ranobe_reader.auth_core.presentation.AuthCoreViewModel
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthUseCaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val savedStateHandler: SavedStateHandle,
    private val getSignedInUsersUseCase: GetSignedInUsersUseCase,
    signInUseCase: SignInUseCase,
    signUpUseCase: SignUpUseCase
) : AuthCoreViewModel(savedStateHandler, signInUseCase, signUpUseCase) {
    val authState = savedStateHandler.getStateFlow<AuthUseCaseState<AuthCheckResult>>(
        key = AUTH_STATE_KEY,
        initialValue = AuthUseCaseState.InProcess
    )

    private fun getSignedInUsers() {
        viewModelScope.launch {
            savedStateHandler[AUTH_STATE_KEY] = AuthUseCaseState.InProcess
            savedStateHandler[AUTH_STATE_KEY] = AuthUseCaseState.ResultReceived(getSignedInUsersUseCase())
        }
    }

    val authProgressBarVisible = savedStateHandler.getStateFlow("auth_progress_bar", false)

    fun switchAuthProgressBar(newValue: Boolean) {
        savedStateHandler["auth_progress_bar"] = newValue
    }

    val uiVisibleFlow = savedStateHandler.getStateFlow(UI_VISIBLE_KEY, false)

    fun switchUiVisible(newValue: Boolean) {
        savedStateHandler[UI_VISIBLE_KEY] = newValue
    }

    private companion object {
        const val AUTH_STATE_KEY = "auth_state"
        const val UI_VISIBLE_KEY = "ui_visible"
    }

    init {
        getSignedInUsers()
    }
}
