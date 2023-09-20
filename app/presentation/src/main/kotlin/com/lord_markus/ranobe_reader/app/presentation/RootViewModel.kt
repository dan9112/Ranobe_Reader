package com.lord_markus.ranobe_reader.app.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lord_markus.ranobe_reader.app.domain.use_cases.GetSettingsDataUseCase
import com.lord_markus.ranobe_reader.core.models.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val savedStateHandler: SavedStateHandle,
    getSettingsDataUseCase: GetSettingsDataUseCase
) : ViewModel() {
    val signedInWithCurrent =
        savedStateHandler.getStateFlow<Pair<List<UserInfo>, Long?>>("users_info", emptyList<UserInfo>() to null)

    fun updateUsersAndCurrent(users: List<UserInfo>, id: Long?) {
        Log.e("MyLog", "Set new value : $users; $id!")
        savedStateHandler["users_info"] = users to id
    }

    val settingsFlow = getSettingsDataUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
