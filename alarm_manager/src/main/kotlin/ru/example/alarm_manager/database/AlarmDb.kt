package ru.example.alarm_manager.database

import androidx.room.*
import kotlinx.datetime.LocalTime

@Entity(
    indices = [
        Index(value = ["title"], unique = true),
        Index(value = ["local_time"], unique = true)
    ]
)
data class AlarmDb(
    @PrimaryKey
    @ColumnInfo(name = "_id")
    val id: Int = 0,
    @ColumnInfo(name = "local_time")
    @TypeConverters(value = [TypeConverter::class])
    val localTime: LocalTime = LocalTime(hour = 0, minute = 0),
    val monday: Boolean = false,
    val tuesday: Boolean = false,
    val wednesday: Boolean = false,
    val thursday: Boolean = false,
    val friday: Boolean = false,
    val saturday: Boolean = false,
    val sunday: Boolean = false,
    val title: String,
    val text: String? = null
)
