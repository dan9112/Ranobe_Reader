package com.lord_markus.ranobe_reader.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lord_markus.ranobe_reader.auth.domain.models.SignInError
import com.lord_markus.ranobe_reader.auth.domain.models.SignInResult
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignUpUseCase
import com.lord_markus.ranobe_reader.auth.presentation.models.UseCaseState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val signInUseCase: SignInUseCase, private val signUpUseCase: SignUpUseCase) : ViewModel() {
    private val _authState = MutableStateFlow<UseCaseState<SignInResult>>(value = UseCaseState.Default)
    val authState: StateFlow<UseCaseState<SignInResult>>
        get() = _authState

    fun tryLogIn(login: String, password: String) {
        viewModelScope.launch {
            _authState.value = UseCaseState.InProcess
            _authState.value = UseCaseState.ResulReceived(
                result = if (login.isBlank() || password.isBlank()) {
                    SignInResult.Error(error = SignInError.IncorrectInput)
                } else {
                    delay(timeMillis = 2100)
                    SignInResult.Error(error = SignInError.NoSuchUser)
                    signInUseCase.invoke(login, password)
                }
            )
        }
    }
}
