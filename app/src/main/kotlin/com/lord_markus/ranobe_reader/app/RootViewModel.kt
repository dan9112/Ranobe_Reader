package com.lord_markus.ranobe_reader.app

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lord_markus.ranobe_reader.core.models.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(private val savedStateHandler: SavedStateHandle) : ViewModel() {
    val signedInWithCurrent =
        savedStateHandler.getStateFlow<Pair<List<UserInfo>, Long?>>("users_info", emptyList<UserInfo>() to null)

    fun updateUsersAndCurrent(users: List<UserInfo>, id: Long?) {
        Log.e("MyLog", "Set new value : $users; $id!")
        savedStateHandler["users_info"] = users to id
    }
}
