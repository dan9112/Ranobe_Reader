package com.lord_markus.ranobe_reader.auth.data.storage.implementation.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUserInfoDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUserInfo

@Dao
interface TableUserInfoDao : ITableUserInfoDao {
    @Query("Select * from users_info where _id = :id")
    override fun getInfoById(id: Long): TableUserInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun addInfo(userInfo: TableUserInfo): Long?
}
