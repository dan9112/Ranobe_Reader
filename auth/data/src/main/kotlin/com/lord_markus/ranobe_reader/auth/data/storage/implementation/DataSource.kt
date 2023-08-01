package com.lord_markus.ranobe_reader.auth.data.storage.implementation

import com.lord_markus.ranobe_reader.auth.data.storage.template.db.IDataSource
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUsersDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUser
import com.lord_markus.ranobe_reader.auth.domain.models.UserState

class DataSource(
    private val tableUsersDao: ITableUsersDao
) : IDataSource {
    override fun getUserInfo(login: String, password: String) =
        tableUsersDao.getUserInfoByLoginAndPassword(login, password)

    override fun addUser(login: String, password: String, state: UserState) = tableUsersDao.addUser(
        TableUser(id = 0L, login, password, state)
    )

    override fun removeUser(id: Long) = tableUsersDao.removeUser(userId = id)
}
