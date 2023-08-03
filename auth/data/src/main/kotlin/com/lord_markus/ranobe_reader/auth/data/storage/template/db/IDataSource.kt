package com.lord_markus.ranobe_reader.auth.data.storage.template.db

import com.lord_markus.ranobe_reader.auth.domain.models.UserInfo
import com.lord_markus.ranobe_reader.auth.domain.models.UserState

interface IDataSource {
    suspend fun signIn(login: String, password: String): UserInfo?
    suspend fun signOut()
    suspend fun addUser(login: String, password: String, state: UserState): Long?
    suspend fun removeUser(id: Long): Int
    suspend fun getSignedIn(): Pair<List<UserInfo>, Long>
    suspend fun setCurrent(id: Long): Boolean?
}
