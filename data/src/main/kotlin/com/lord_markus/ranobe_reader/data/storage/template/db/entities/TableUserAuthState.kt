package com.lord_markus.ranobe_reader.data.storage.template.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.lord_markus.ranobe_reader.data.storage.template.db.entities.TableUser

@Entity(
    tableName = "users_auth_state",
    foreignKeys = [
        ForeignKey(
            entity = TableUser::class,
            parentColumns = ["_id"],
            childColumns = ["_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TableUserAuthState(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    val id: Long,
    @ColumnInfo(name = "auth_state")
    val authState: Boolean = false
)
