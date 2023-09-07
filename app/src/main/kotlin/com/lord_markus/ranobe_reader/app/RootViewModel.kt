package com.lord_markus.ranobe_reader.app

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lord_markus.ranobe_reader.core.models.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(private val savedStateHandler: SavedStateHandle) : ViewModel() {
    val signedInWithCurrent =
        savedStateHandler.getStateFlow<Pair<List<UserInfo>, Long?>>("users_info", emptyList<UserInfo>() to null)

    fun updateUsersAndCurrent(users: List<UserInfo>, id: Long?) {
        signedInWithCurrent.value.run {
            if (users != first || id != second)
                if (first.isEmpty() && users.isNotEmpty()) {
                    navigate.value = true
                } else if (first.isNotEmpty() && users.isEmpty()) {
                    navigate.value = false
                }
            savedStateHandler["users_info"] = users to id
        }
    }

    val navigate = MutableStateFlow<Boolean?>(null)
}
