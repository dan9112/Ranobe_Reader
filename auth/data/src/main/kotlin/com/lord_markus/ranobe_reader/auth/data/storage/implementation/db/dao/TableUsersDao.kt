package com.lord_markus.ranobe_reader.auth.data.storage.implementation.db.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUsersDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUser
import com.lord_markus.ranobe_reader.auth.domain.models.UserInfo

interface TableUsersDao : ITableUsersDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    override fun addUser(user: TableUser): Long?

    @Query("Select * from users where login = :login and password = :password")
    override fun getUserInfoByLoginAndPassword(login: String, password: String): UserInfo?

    @Query("Delete from users where _id = :userId")
    override fun removeUser(userId: Long): Int
}
