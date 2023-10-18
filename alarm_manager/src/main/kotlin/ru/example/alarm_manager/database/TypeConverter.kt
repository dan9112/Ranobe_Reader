package ru.example.alarm_manager.database

import androidx.room.TypeConverter
import kotlinx.datetime.LocalTime

class TypeConverter {

    @TypeConverter
    fun fromInt(int: Int) = LocalTime(hour = int / SIXTY, minute = int % SIXTY)

    @TypeConverter
    fun LocalTime.toInt() = hour * SIXTY + minute

    private companion object {
        const val SIXTY = 60
    }
}
