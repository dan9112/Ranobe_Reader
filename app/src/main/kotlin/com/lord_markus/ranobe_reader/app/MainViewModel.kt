package com.lord_markus.ranobe_reader.app

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lord_markus.ranobe_reader.core.models.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val savedStateHandler: SavedStateHandle) : ViewModel() {
    val userInfo = savedStateHandler.getStateFlow<List<UserInfo>>(key = USERS_STATE_KEY, initialValue = emptyList())

    fun updateSignedIn(newList: List<UserInfo>) {
        savedStateHandler[USERS_STATE_KEY] = newList
    }

    private companion object {
        const val USERS_STATE_KEY = "user_state"
    }
}
