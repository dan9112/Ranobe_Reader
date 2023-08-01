package com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.lord_markus.ranobe_reader.auth.data.storage.implementation.db.type_converters.UserStateTypeConverter
import com.lord_markus.ranobe_reader.auth.domain.models.UserState

@Entity(
    tableName = "users",
    indices = [Index(value = ["login"], unique = true)]
)
data class TableUser(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long,
    var login: String,
    var password: String,
    @TypeConverters(UserStateTypeConverter::class)
    val state: UserState = UserState.User
)
