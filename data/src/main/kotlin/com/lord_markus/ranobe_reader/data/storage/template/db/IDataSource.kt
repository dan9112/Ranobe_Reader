package com.lord_markus.ranobe_reader.data.storage.template.db

import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState
import com.lord_markus.ranobe_reader.settings.domain.models.SettingsData
import kotlinx.coroutines.flow.Flow
import com.lord_markus.ranobe_reader.app.domain.models.SettingsData as AppSettingsData

interface IDataSource {
    suspend fun signIn(login: String, password: String, update: Boolean): UserInfo?
    suspend fun signOut(): List<UserInfo>
    suspend fun addUser(login: String, password: String, state: UserState, withSignIn: Boolean): Long?
    suspend fun signOutWithRemove(): List<UserInfo>
    suspend fun getSignedIn(): Pair<List<UserInfo>, Long>?
    suspend fun setCurrent(id: Long): Boolean?
    val settingsDataFlow: Flow<AppSettingsData>
    suspend fun setSettings(settingsData: SettingsData)
    suspend fun setDynamicColor(on: Boolean)
    suspend fun setNightMode(flag: Boolean?)
}
