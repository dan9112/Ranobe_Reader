package ru.example.alarm_manager.database

import android.database.sqlite.SQLiteConstraintException
import androidx.room.*

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    @Throws(SQLiteConstraintException::class)
    @TypeConverters(value = [TypeConverter::class])
    fun insert(alarmDb: AlarmDb): Long

    @Query("Delete from alarmdb where _id = :id")
    fun delete(id: Int): Int

    @Query("Select * from alarmdb")
    @TypeConverters(value = [TypeConverter::class])
    fun getAll(): List<AlarmDb>

    @Update
    @Throws(SQLiteConstraintException::class)
    @TypeConverters(value = [TypeConverter::class])
    fun update(alarmDb: AlarmDb)

    @Query("SELECT * FROM alarmdb WHERE title LIKE :title LIMIT 1")
    @TypeConverters(value = [TypeConverter::class])
    fun findByName(title: String): AlarmDb?

    @Query("Select MAX(_id) from alarmdb")
    fun getLastId(): Int?
}
