package com.lord_markus.ranobe_reader.auth.data.storage.implementation.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUserDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUser

@Dao
interface TableUserDao : ITableUserDao {
    @Insert(onConflict = IGNORE)
    override fun addUser(user: TableUser): Long

    @Query("Delete from users where _id = :userId")
    override fun removeUser(userId: Long): Int

    @Query("Select _id from users where login = :login and password = :password")
    override fun getId(login: String, password: String): Long?
}
