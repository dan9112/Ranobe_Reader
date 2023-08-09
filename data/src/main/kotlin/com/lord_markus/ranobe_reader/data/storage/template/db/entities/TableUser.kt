package com.lord_markus.ranobe_reader.data.storage.template.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["login"], unique = true)]
)
data class TableUser(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    val id: Long,
    var login: String,
    var password: String
)
