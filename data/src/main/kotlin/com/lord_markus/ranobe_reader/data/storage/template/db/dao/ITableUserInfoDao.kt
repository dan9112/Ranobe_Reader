package com.lord_markus.ranobe_reader.data.storage.template.db.dao

import com.lord_markus.ranobe_reader.data.storage.template.db.entities.TableUserInfo

interface ITableUserInfoDao {
    fun getInfoById(id: Long): TableUserInfo?
    fun addInfo(userInfo: TableUserInfo): Long?
}
