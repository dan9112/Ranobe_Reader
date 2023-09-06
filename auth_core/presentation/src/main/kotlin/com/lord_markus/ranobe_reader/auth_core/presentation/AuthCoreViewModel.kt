package com.lord_markus.ranobe_reader.auth_core.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lord_markus.ranobe_reader.auth_core.domain.models.SignInError
import com.lord_markus.ranobe_reader.auth_core.domain.models.SignInResultAuth
import com.lord_markus.ranobe_reader.auth_core.domain.models.SignUpError
import com.lord_markus.ranobe_reader.auth_core.domain.models.SignUpResultAuth
import com.lord_markus.ranobe_reader.auth_core.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth_core.domain.use_cases.SignUpUseCase
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthScreenState
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthUseCaseState
import com.lord_markus.ranobe_reader.auth_core.presentation.models.ExtendedAuthUseCaseState
import com.lord_markus.ranobe_reader.core.models.UserState
import kotlinx.coroutines.launch

abstract class AuthCoreViewModel(
    private val savedStateHandler: SavedStateHandle,
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    val authScreenState = savedStateHandler.getStateFlow<AuthScreenState>(
        key = AUTH_SCREEN_STATE_KEY,
        initialValue = AuthScreenState.SignIn
    )
    val signInState = savedStateHandler.getStateFlow<ExtendedAuthUseCaseState<SignInResultAuth>>(
        key = SIGN_IN_STATE_KEY,
        initialValue = ExtendedAuthUseCaseState.Default
    )
    val signUpState = savedStateHandler.getStateFlow<ExtendedAuthUseCaseState<SignUpResultAuth>>(
        key = SIGN_UP_STATE_KEY,
        initialValue = ExtendedAuthUseCaseState.Default
    )

    fun trySignIn(login: String, password: String, update: Boolean = true) {
        viewModelScope.launch {
            savedStateHandler[SIGN_IN_STATE_KEY] = AuthUseCaseState.InProcess
            savedStateHandler[SIGN_IN_STATE_KEY] = AuthUseCaseState.ResultReceived(
                result = if (login.isBlank() || password.isBlank()) {
                    SignInResultAuth.Error(error = SignInError.IncorrectInput)
                } else {
                    signInUseCase(login, password, update)
                }
            )
        }
    }

    fun trySignUp(login: String, password: String, password2: String) {
        if (password2 != password) {
            savedStateHandler[SIGN_UP_STATE_KEY] =
                AuthUseCaseState.ResultReceived(
                    result = SignUpResultAuth.Error(error = SignUpError.IncorrectInput)
                )
        } else {
            viewModelScope.launch {
                savedStateHandler[SIGN_UP_STATE_KEY] = AuthUseCaseState.InProcess
                savedStateHandler[SIGN_UP_STATE_KEY] = AuthUseCaseState.ResultReceived(
                    result = if (login.isBlank() || password.isBlank()) {
                        SignUpResultAuth.Error(error = SignUpError.IncorrectInput)
                    } else {
                        signUpUseCase(login = login, password = password, userState = UserState.User, withSignIn = true)
                    }
                )
            }
        }
    }

    fun switchAuthScreenState() {
        savedStateHandler[AUTH_SCREEN_STATE_KEY] =
            if (authScreenState.value == AuthScreenState.SignIn) AuthScreenState.SignUp else AuthScreenState.SignIn
    }

    fun resetSignInTrigger() {
        val currentAuthState = signInState.value
        if (currentAuthState is AuthUseCaseState.ResultReceived && currentAuthState.result is SignInResultAuth.Error)
            savedStateHandler[SIGN_IN_STATE_KEY] =
                AuthUseCaseState.ResultReceived(currentAuthState.result.copy(trigger = false))
    }

    val authCoreProgressBarVisible = savedStateHandler.getStateFlow(AUTH_CORE_PROGRESS_BAR_KEY, false)

    fun switchAuthCoreProgressBar(newValue: Boolean) {
        savedStateHandler[AUTH_CORE_PROGRESS_BAR_KEY] = newValue
    }

    fun resetSignUpTrigger() {
        savedStateHandler[SIGN_IN_STATE_KEY] = ExtendedAuthUseCaseState.Default
    }

    protected companion object {
        const val AUTH_SCREEN_STATE_KEY = "auth_screen_state"
        const val SIGN_IN_STATE_KEY = "sign_in_state"
        const val SIGN_UP_STATE_KEY = "sign_up_state"
        const val AUTH_CORE_PROGRESS_BAR_KEY = "auth_core_progress_bar"
    }
}
