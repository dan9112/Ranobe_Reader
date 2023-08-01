package com.lord_markus.ranobe_reader.auth.data.storage.implementation.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.IAppDatabase
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUsersDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUser

@Database(entities = [TableUser::class], version = 1)
abstract class AppDatabase : RoomDatabase(), IAppDatabase {
    abstract override fun tableUsersDao(): ITableUsersDao
}
