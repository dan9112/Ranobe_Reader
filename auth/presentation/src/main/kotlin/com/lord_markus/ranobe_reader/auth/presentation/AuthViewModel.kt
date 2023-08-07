package com.lord_markus.ranobe_reader.auth.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lord_markus.ranobe_reader.auth.domain.models.SignInError
import com.lord_markus.ranobe_reader.auth.domain.models.SignInResult
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignUpUseCase
import com.lord_markus.ranobe_reader.auth.presentation.models.UseCaseState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthViewModel(
    private val savedStateHandler: SavedStateHandle,
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    val authState = savedStateHandler.getStateFlow<UseCaseState<SignInResult>>("auth_state", UseCaseState.Default)

    fun tryLogIn(login: String, password: String) {
        viewModelScope.launch {
            savedStateHandler["auth_state"] = UseCaseState.InProcess
            savedStateHandler["auth_state"] = UseCaseState.ResultReceived(
                result = if (login.isBlank() || password.isBlank()) {
                    SignInResult.Error(error = SignInError.IncorrectInput)
                } else {
                    delay(timeMillis = 4100)
                    SignInResult.Error(error = SignInError.NoSuchUser)
                    signInUseCase.invoke(login, password)
                }
            )
        }
    }

    fun caughtTrigger() {
        val currentAuthState = authState.value
        if (currentAuthState is UseCaseState.ResultReceived) savedStateHandler["auth_state"] =
            currentAuthState.apply {
                trigger = false
            }
    }

    init {
        Log.e("MyLog", "ViewModel init")
    }
}
