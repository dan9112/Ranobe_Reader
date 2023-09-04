package com.lord_markus.ranobe_reader.auth.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth.domain.use_cases.GetSignedInUsersUseCase
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthUseCaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val savedStateHandler: SavedStateHandle,
    private val getSignedInUsersUseCase: GetSignedInUsersUseCase
) : ViewModel() {
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

    private companion object {
        const val AUTH_STATE_KEY = "auth_state"
    }

    init {
        getSignedInUsers()
    }
}
