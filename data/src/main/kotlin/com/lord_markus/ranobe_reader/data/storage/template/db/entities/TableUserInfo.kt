package com.lord_markus.ranobe_reader.data.storage.template.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.lord_markus.ranobe_reader.core.models.UserState

@Entity(
    tableName = "users_info",
    foreignKeys = [
        ForeignKey(
            entity = TableUser::class,
            parentColumns = ["_id"],
            childColumns = ["_id"],
            onDelete = CASCADE
        )
    ]
)
data class TableUserInfo(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    val id: Long,
    val name: String,
    val state: UserState = UserState.User
)
