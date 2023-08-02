package com.lord_markus.ranobe_reader.auth.data.storage.implementation

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.IAppDatabase
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.IDataSource
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUserAuthStateDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUserDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUserInfoDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUser
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUserAuthState
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUserInfo
import com.lord_markus.ranobe_reader.auth.domain.models.UserInfo
import com.lord_markus.ranobe_reader.auth.domain.models.UserState
import java.io.IOException
import java.util.concurrent.Callable

class DataSource(
    private val tableUsersDao: ITableUserDao,
    private val tableUserInfoDao: ITableUserInfoDao,
    private val tableUserAuthState: ITableUserAuthStateDao,
    private val database: IAppDatabase,
    private val context: Context
) : IDataSource {
    private val sharedPreferences
        get() = context.getSharedPreferences("users", MODE_PRIVATE)

    private fun <T> T.updateSharedPreferences(
        dataChange: SharedPreferences.Editor.(T) -> SharedPreferences.Editor
    ) = also {
        sharedPreferences
            .edit()
            .dataChange(it)
            .apply()
    }

    override fun signIn(login: String, password: String) = database.runInTransaction(
        body = Callable {
            tableUsersDao.getId(login, password)
                ?.let { id ->
                    tableUserInfoDao.getInfoById(id)
                        .run {
                            if (this == null) throw IOException("Couldn't get user info for id = $id")
                            UserInfo(id, state)
                        }
                        .apply {
                            tableUserAuthState.addState(
                                userState = TableUserAuthState(id, true)
                            ) ?: throw IOException("Couldn't update signed in list for id = $id")
                        }
                        .updateSharedPreferences {
                            putLong(CURRENT_USER_ID_KEY, id)
                        }
                }
        }
    )

    override fun signOut() {
        if (sharedPreferences.contains(CURRENT_USER_ID_KEY)) {
            val id = sharedPreferences.getLong(CURRENT_USER_ID_KEY, 1L)
            if (tableUserAuthState.removeUserById(id) > 0) {
                updateSharedPreferences {
                    remove(CURRENT_USER_ID_KEY)
                }
            } else {
                updateSharedPreferences {
                    remove(CURRENT_USER_ID_KEY)
                }
                throw IOException("No such user with id = $id!")
            }
        } else throw IOException("No signed in users")
    }

    override fun addUser(
        login: String,
        password: String,
        state: UserState
    ) = database.runInTransaction(
        body = Callable {
            tableUsersDao.addUser(user = TableUser(id = 0L, login, password)).let { id ->
                if (id < 0) {
                    null
                } else {
                    tableUserInfoDao.addInfo(userInfo = TableUserInfo(id, state)) ?: return@let null
                    tableUserAuthState.addState(userState = TableUserAuthState(id))
                        ?: return@let null
                    id
                }
            }
        }
    )

    override fun removeUser(id: Long) = tableUsersDao
        .removeUser(userId = id)
        .updateSharedPreferences {
            remove(CURRENT_USER_ID_KEY)
        }

    override fun getSignedIn() = database.runInTransaction(
        body = Callable {
            tableUserAuthState.getAllSignedIn().map {
                tableUserInfoDao.getInfoById(id = it)?.run {
                    UserInfo(id, state)
                } ?: throw IOException("Caught user without info: id = $it!")
            }.let { usersInfo ->
                usersInfo to if (sharedPreferences.contains(CURRENT_USER_ID_KEY)) {
                    sharedPreferences.getLong(CURRENT_USER_ID_KEY, -1L)
                } else {// никогда не должен срабатывать, страховка!
                    usersInfo.first().id.also { id ->
                        updateSharedPreferences {
                            putLong(CURRENT_USER_ID_KEY, id)
                        }
                    }
                }
            }
        }
    )

    override fun setCurrent(id: Long) = tableUserAuthState.getAuthStateById(id)?.also {
        if (it) updateSharedPreferences {
            putLong(CURRENT_USER_ID_KEY, id)
        }
    }

    private companion object {
        const val CURRENT_USER_ID_KEY = "current_user_key"
    }
}
