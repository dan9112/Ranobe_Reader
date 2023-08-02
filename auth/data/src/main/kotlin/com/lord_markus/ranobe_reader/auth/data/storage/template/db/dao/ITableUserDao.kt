package com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao

import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUser

interface ITableUserDao {
    fun getId(login: String, password: String): Long?
    fun addUser(user: TableUser): Long
    fun removeUser(userId: Long): Int
}
