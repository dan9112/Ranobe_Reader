package com.lord_markus.ranobe_reader.app

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lord_markus.ranobe_reader.core.models.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val savedStateHandler: SavedStateHandle) : ViewModel() {
    val userInfo = savedStateHandler.getStateFlow<UserState?>(key = USERS_STATE_KEY, initialValue = null)
    fun removeUserInfo() {
        savedStateHandler[USERS_STATE_KEY] = null
    }

    fun setUsersInfo(currentState: UserState) {
        savedStateHandler[USERS_STATE_KEY] = currentState
    }

    private companion object {
        const val USERS_STATE_KEY = "user_state"
    }
}
