package com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao

import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUser
import com.lord_markus.ranobe_reader.auth.domain.models.UserInfo

interface ITableUsersDao {
    fun getUserInfoByLoginAndPassword(login: String, password: String): UserInfo?
    fun addUser(user: TableUser): Long?
    fun removeUser(userId: Long): Int
}
