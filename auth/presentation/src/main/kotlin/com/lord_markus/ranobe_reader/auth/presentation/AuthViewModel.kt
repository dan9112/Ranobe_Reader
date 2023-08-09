package com.lord_markus.ranobe_reader.auth.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lord_markus.ranobe_reader.auth.domain.models.*
import com.lord_markus.ranobe_reader.auth.domain.use_cases.GetSignedInUsersUseCase
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignUpUseCase
import com.lord_markus.ranobe_reader.auth.presentation.models.AuthScreenState
import com.lord_markus.ranobe_reader.auth.presentation.models.ExtendedUseCaseState
import com.lord_markus.ranobe_reader.auth.presentation.models.UseCaseState
import com.lord_markus.ranobe_reader.core.models.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val savedStateHandler: SavedStateHandle,
    private val getSignedInUsersUseCase: GetSignedInUsersUseCase,
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    val authState = savedStateHandler.getStateFlow<UseCaseState<AuthCheckResultAuth>>(
        key = AUTH_STATE_KEY,
        initialValue = UseCaseState.InProcess
    )
    val authScreenState = savedStateHandler.getStateFlow<AuthScreenState>(
        key = AUTH_SCREEN_STATE_KEY,
        initialValue = AuthScreenState.SignIn
    )
    val signInState = savedStateHandler.getStateFlow<ExtendedUseCaseState<SignInResultAuth>>(
        key = SIGN_IN_STATE_KEY,
        initialValue = ExtendedUseCaseState.Default
    )
    val signUpState = savedStateHandler.getStateFlow<ExtendedUseCaseState<SignUpResultAuth>>(
        key = SIGN_UP_STATE_KEY,
        initialValue = ExtendedUseCaseState.Default
    )

    fun trySignIn(login: String, password: String) {
        viewModelScope.launch {
            savedStateHandler[SIGN_IN_STATE_KEY] = UseCaseState.InProcess
            savedStateHandler[SIGN_IN_STATE_KEY] = UseCaseState.ResultReceived(
                result = if (login.isBlank() || password.isBlank()) {
                    SignInResultAuth.Error(error = SignInError.IncorrectInput)
                } else {
                    signInUseCase(login, password)
                }
            )
        }
    }

    fun trySignUp(login: String, password: String, password2: String) {
        if (password2 != password) {
            savedStateHandler[SIGN_UP_STATE_KEY] =
                UseCaseState.ResultReceived(
                    result = SignUpResultAuth.Error(error = SignUpError.IncorrectInput)
                )
        } else {
            viewModelScope.launch {
                savedStateHandler[SIGN_UP_STATE_KEY] = UseCaseState.InProcess
                savedStateHandler[SIGN_UP_STATE_KEY] = UseCaseState.ResultReceived(
                    result = if (login.isBlank() || password.isBlank()) {
                        SignUpResultAuth.Error(error = SignUpError.IncorrectInput)
                    } else {
                        signUpUseCase(login, password, UserState.User)
                    }
                )
            }
        }
    }

    private fun getSignedInUsers() {
        savedStateHandler[AUTH_STATE_KEY] = UseCaseState.InProcess
        viewModelScope.launch {
            savedStateHandler[AUTH_STATE_KEY] = UseCaseState.ResultReceived(getSignedInUsersUseCase())
        }
    }

    fun switchAuthScreenState() {
        savedStateHandler[AUTH_SCREEN_STATE_KEY] =
            if (authScreenState.value == AuthScreenState.SignIn) AuthScreenState.SignUp else AuthScreenState.SignIn
    }

    fun caughtTrigger() {
        val currentAuthState = signInState.value
        if (currentAuthState is UseCaseState.ResultReceived) savedStateHandler[SIGN_IN_STATE_KEY] =
            currentAuthState.apply {
                trigger = false
            }
    }

    private companion object {
        const val AUTH_SCREEN_STATE_KEY = "auth_screen_state"
        const val AUTH_STATE_KEY = "auth_state"
        const val SIGN_IN_STATE_KEY = "sign_in_state"
        const val SIGN_UP_STATE_KEY = "sign_up_state"
    }

    init {
        getSignedInUsers()
    }
}
