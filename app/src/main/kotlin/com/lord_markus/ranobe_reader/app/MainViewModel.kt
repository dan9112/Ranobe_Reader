package com.lord_markus.ranobe_reader.app

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lord_markus.ranobe_reader.core.models.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val savedStateHandler: SavedStateHandle) : ViewModel() {


    val userInfo = savedStateHandler.getStateFlow<Pair<List<UserInfo>, Long>>(
        key = USERS_STATE_KEY,
        initialValue = emptyList<UserInfo>() to -1
    )

    fun updateSignedIn(newList: List<UserInfo>) {
        savedStateHandler[USERS_STATE_KEY] = userInfo.value.copy(first = newList)
    }

    fun updateCurrentSignedIn(newId: Long) {
        savedStateHandler[USERS_STATE_KEY] = userInfo.value.copy(second = newId)
    }

    private companion object {
        const val USERS_STATE_KEY = "user_state"
    }
}
