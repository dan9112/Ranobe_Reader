package com.lord_markus.ranobe_reader.auth.data.storage.template.db

import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUserAuthStateDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUserDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUserInfoDao

interface IAppDatabase : Transactionable {
    fun tableUserDao(): ITableUserDao
    fun tableUserInfoDao(): ITableUserInfoDao
    fun tableUserAuthStateDao(): ITableUserAuthStateDao
}
