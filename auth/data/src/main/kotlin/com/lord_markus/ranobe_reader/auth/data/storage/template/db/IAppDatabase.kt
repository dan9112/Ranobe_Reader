package com.lord_markus.ranobe_reader.auth.data.storage.template.db

import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUsersDao

interface IAppDatabase {
    fun tableUsersDao(): ITableUsersDao
}