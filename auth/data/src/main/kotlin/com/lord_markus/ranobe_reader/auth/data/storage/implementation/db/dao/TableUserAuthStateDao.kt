package com.lord_markus.ranobe_reader.auth.data.storage.implementation.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUserAuthStateDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUserAuthState

@Dao
interface TableUserAuthStateDao : ITableUserAuthStateDao {
    @Query("Select auth_state from users_auth_state where _id = :id")
    override fun getAuthStateById(id: Long): Boolean?

    @Query("Select _id from users_auth_state where auth_state = 1")
    override fun getAllSignedIn(): List<Long>

    @Insert(onConflict = REPLACE)
    override fun addState(userState: TableUserAuthState): Long?

    @Query("Delete from users_auth_state where _id = :id")
    override fun removeUserById(id: Long): Int
}
