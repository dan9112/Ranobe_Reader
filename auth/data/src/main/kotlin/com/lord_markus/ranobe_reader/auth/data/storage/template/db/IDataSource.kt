package com.lord_markus.ranobe_reader.auth.data.storage.template.db

import com.lord_markus.ranobe_reader.auth.domain.models.UserInfo
import com.lord_markus.ranobe_reader.auth.domain.models.UserState

interface IDataSource {
    fun getUserInfo(login: String, password: String): UserInfo?
    fun addUser(login: String, password: String, state: UserState): Long?
    fun removeUser(id: Long): Int
}
