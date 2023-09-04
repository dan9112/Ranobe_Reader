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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthCoreViewModel @Inject constructor(
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

    fun trySignIn(login: String, password: String, update: Boolean) {
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

    fun caughtTrigger() {
        val currentAuthState = signInState.value
        if (currentAuthState is AuthUseCaseState.ResultReceived) savedStateHandler[SIGN_IN_STATE_KEY] =
            currentAuthState.apply {
                trigger = false
            }
    }

    private companion object {
        const val AUTH_SCREEN_STATE_KEY = "auth_screen_state"
        const val SIGN_IN_STATE_KEY = "sign_in_state"
        const val SIGN_UP_STATE_KEY = "sign_up_state"
    }
}
