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
import kotlinx.coroutines.launch

class AuthViewModel(
    private val savedStateHandler: SavedStateHandle,
    private val getSignedInUsersUseCase: GetSignedInUsersUseCase,
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    val authState = savedStateHandler.getStateFlow<UseCaseState<AuthCheckResult>>(
        key = authStateKey,
        initialValue = UseCaseState.InProcess
    )
    val authScreenState = savedStateHandler.getStateFlow<AuthScreenState>(
        key = authScreenStateKey,
        initialValue = AuthScreenState.SignIn
    )
    val signInState = savedStateHandler.getStateFlow<ExtendedUseCaseState<SignInResult>>(
        key = signInStateKey,
        initialValue = ExtendedUseCaseState.Default
    )
    val signUpState = savedStateHandler.getStateFlow<ExtendedUseCaseState<SignUpResult>>(
        key = signUpStateKey,
        initialValue = ExtendedUseCaseState.Default
    )

    fun trySignIn(login: String, password: String) {
        viewModelScope.launch {
            savedStateHandler[signInStateKey] = UseCaseState.InProcess
            savedStateHandler[signInStateKey] = UseCaseState.ResultReceived(
                result = if (login.isBlank() || password.isBlank()) {
                    SignInResult.Error(error = SignInError.IncorrectInput)
                } else {
                    signInUseCase(login, password)
                }
            )
        }
    }

    fun trySignUp(login: String, password: String, password2: String) {
        if (password2 != password) {
            savedStateHandler[signUpStateKey] =
                UseCaseState.ResultReceived(
                    result = SignUpResult.Error(error = SignUpError.IncorrectInput)
                )
        } else {
            viewModelScope.launch {
                savedStateHandler[signUpStateKey] = UseCaseState.InProcess
                savedStateHandler[signUpStateKey] = UseCaseState.ResultReceived(
                    result = if (login.isBlank() || password.isBlank()) {
                        SignUpResult.Error(error = SignUpError.IncorrectInput)
                    } else {
                        signUpUseCase(login, password, UserState.User)
                    }
                )
            }
        }
    }

    fun getSignedInUsers() {
        savedStateHandler[authStateKey] = UseCaseState.InProcess
        viewModelScope.launch {
            savedStateHandler[authStateKey] = UseCaseState.ResultReceived(getSignedInUsersUseCase())
        }
    }

    fun switchAuthScreenState() {
        savedStateHandler[authScreenStateKey] =
            if (authScreenState.value == AuthScreenState.SignIn) AuthScreenState.SignUp else AuthScreenState.SignIn
    }

    fun caughtTrigger() {
        val currentAuthState = signInState.value
        if (currentAuthState is UseCaseState.ResultReceived) savedStateHandler[signInStateKey] =
            currentAuthState.apply {
                trigger = false
            }
    }

    private companion object {
        const val authScreenStateKey = "auth_screen_state"
        const val authStateKey = "auth_state"
        const val signInStateKey = "sign_in_state"
        const val signUpStateKey = "sign_up_state"
    }

    init {
        getSignedInUsers()
    }
}
