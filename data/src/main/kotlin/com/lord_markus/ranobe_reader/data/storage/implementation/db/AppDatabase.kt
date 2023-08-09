package com.lord_markus.ranobe_reader.data.storage.implementation.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lord_markus.ranobe_reader.data.storage.implementation.db.dao.TableUserAuthStateDao
import com.lord_markus.ranobe_reader.data.storage.implementation.db.dao.TableUserDao
import com.lord_markus.ranobe_reader.data.storage.implementation.db.dao.TableUserInfoDao
import com.lord_markus.ranobe_reader.data.storage.implementation.db.type_converters.UserStateTypeConverter
import com.lord_markus.ranobe_reader.data.storage.template.db.IAppDatabase
import com.lord_markus.ranobe_reader.data.storage.template.db.entities.TableUser
import com.lord_markus.ranobe_reader.data.storage.template.db.entities.TableUserAuthState
import com.lord_markus.ranobe_reader.data.storage.template.db.entities.TableUserInfo

@Database(
    entities = [TableUser::class, TableUserInfo::class, TableUserAuthState::class],
    version = 1
)
@TypeConverters(UserStateTypeConverter::class)
abstract class AppDatabase : RoomDatabase(), IAppDatabase {
    abstract override fun tableUserDao(): TableUserDao
    abstract override fun tableUserInfoDao(): TableUserInfoDao
    abstract override fun tableUserAuthStateDao(): TableUserAuthStateDao

    companion object {
        fun create(context: Context): IAppDatabase = Room
            .databaseBuilder(
                context = context,
                klass = AppDatabase::class.java,
                name = "auth_database"
            )
            .build()
    }
}
