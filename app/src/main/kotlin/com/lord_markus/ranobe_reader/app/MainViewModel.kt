package com.lord_markus.ranobe_reader.app

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lord_markus.ranobe_reader.core.models.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val savedStateHandler: SavedStateHandle) : ViewModel() {
    val userInfo = savedStateHandler.getStateFlow<Pair<Long, List<UserInfo>>?>(key = usersInfoKey, initialValue = null)
    fun removeUserInfo() {
        savedStateHandler[usersInfoKey] = null
    }

    fun setUsersInfo(current: Long, all: List<UserInfo>) {
        savedStateHandler[usersInfoKey] = current to all
    }

    private companion object {
        const val usersInfoKey = "users_info"
    }
}
