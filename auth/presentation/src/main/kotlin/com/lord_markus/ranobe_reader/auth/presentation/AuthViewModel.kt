package com.lord_markus.ranobe_reader.auth.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lord_markus.ranobe_reader.auth.domain.models.SignInError
import com.lord_markus.ranobe_reader.auth.domain.models.SignInResult
import com.lord_markus.ranobe_reader.auth.domain.models.SignUpError
import com.lord_markus.ranobe_reader.auth.domain.models.SignUpResult
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignUpUseCase
import com.lord_markus.ranobe_reader.auth.presentation.models.AuthScreenState
import com.lord_markus.ranobe_reader.auth.presentation.models.UseCaseState
import com.lord_markus.ranobe_reader.core.UserState
import kotlinx.coroutines.launch

class AuthViewModel(
    private val savedStateHandler: SavedStateHandle,
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    val authScreenState = savedStateHandler.getStateFlow<AuthScreenState>("auth_state", AuthScreenState.SignIn)
    val signInState = savedStateHandler.getStateFlow<UseCaseState<SignInResult>>("sign_in_state", UseCaseState.Default)
    val signUpState = savedStateHandler.getStateFlow<UseCaseState<SignUpResult>>("sign_up_state", UseCaseState.Default)

    fun trySignIn(login: String, password: String) {
        viewModelScope.launch {
            savedStateHandler["sign_in_state"] = UseCaseState.InProcess
            savedStateHandler["sign_in_state"] = UseCaseState.ResultReceived(
                result = if (login.isBlank() || password.isBlank()) {
                    SignInResult.Error(error = SignInError.IncorrectInput)
                } else {
                    signInUseCase.invoke(login, password)
                }
            )
        }
    }

    fun trySignUp(login: String, password: String, password2: String) {
        if (password2 != password) {
            savedStateHandler["sign_up_state"] =
                UseCaseState.ResultReceived(result = SignUpResult.Error(error = SignUpError.IncorrectInput))
        } else {
            viewModelScope.launch {
                savedStateHandler["sign_up_state"] = UseCaseState.InProcess
                savedStateHandler["sign_up_state"] = UseCaseState.ResultReceived(
                    result = if (login.isBlank() || password.isBlank()) {
                        SignUpResult.Error(error = SignUpError.IncorrectInput)
                    } else {
                        signUpUseCase.invoke(login, password, UserState.User)
                    }
                )
            }
        }
    }

    fun switchAuthScreenState() {
        savedStateHandler["auth_state"] =
            if (authScreenState.value == AuthScreenState.SignIn) AuthScreenState.SignUp else AuthScreenState.SignIn
    }

    fun caughtTrigger() {
        val currentAuthState = signInState.value
        if (currentAuthState is UseCaseState.ResultReceived) savedStateHandler["sign_in_state"] =
            currentAuthState.apply {
                trigger = false
            }
    }

    init {
        Log.e("MyLog", "ViewModel init")
    }
}
