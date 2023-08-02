package com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao

import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUserAuthState

interface ITableUserAuthStateDao {
    fun getAuthStateById(id: Long): Boolean?
    fun getAllSignedIn(): List<Long>
    fun addState(userState: TableUserAuthState): Long?
    fun removeUserById(id: Long): Int
}