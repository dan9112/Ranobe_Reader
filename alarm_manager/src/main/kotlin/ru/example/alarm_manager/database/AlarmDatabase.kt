package ru.example.alarm_manager.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [AlarmDb::class], version = 1, exportSchema = false)
@TypeConverters(TypeConverter::class)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
