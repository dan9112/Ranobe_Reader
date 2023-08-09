package com.lord_markus.ranobe_reader.data.storage.template.db

import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState

interface IDataSource {
    suspend fun signIn(login: String, password: String): UserInfo?
    suspend fun signOut(): List<UserInfo>
    suspend fun addUser(login: String, password: String, state: UserState): Long?
    suspend fun signOutWithRemove(): List<UserInfo>
    suspend fun getSignedIn(): Pair<List<UserInfo>, Long>?
    suspend fun setCurrent(id: Long): Boolean?
}
