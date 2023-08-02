package com.lord_markus.ranobe_reader.auth.data.storage.template.db

import com.lord_markus.ranobe_reader.auth.domain.models.UserInfo
import com.lord_markus.ranobe_reader.auth.domain.models.UserState

interface IDataSource {
    fun signIn(login: String, password: String): UserInfo?
    fun signOut()
    fun addUser(login: String, password: String, state: UserState): Long?

    fun removeUser(id: Long): Int

    fun getSignedIn(): Pair<List<UserInfo>, Long>

    fun setCurrent(id: Long): Boolean?
}
