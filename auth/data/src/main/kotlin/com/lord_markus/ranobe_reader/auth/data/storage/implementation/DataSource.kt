package com.lord_markus.ranobe_reader.auth.data.storage.implementation

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.lord_markus.ranobe_reader.auth.data.R
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.IAppDatabase
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.IDataSource
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUserAuthStateDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUserDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUserInfoDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUser
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUserAuthState
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUserInfo
import com.lord_markus.ranobe_reader.core.UserInfo
import com.lord_markus.ranobe_reader.core.UserState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.Callable

class DataSource(
    private val tableUsersDao: ITableUserDao,
    private val tableUserInfoDao: ITableUserInfoDao,
    private val tableUserAuthState: ITableUserAuthStateDao,
    private val database: IAppDatabase,
    private val context: Context,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
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

    override suspend fun signIn(login: String, password: String) = withContext(defaultDispatcher) {
        database.runInTransaction(
            body = Callable {
                tableUsersDao.getId(login, password)
                    ?.let { id ->
                        tableUserInfoDao.getInfoById(id)
                            .run {
                                if (this == null) throw IOException(
                                    context.getString(
                                        R.string.couldn_t_get_user_info_for_id,
                                        id
                                    )
                                )
                                UserInfo(id, state)
                            }
                            .apply {
                                tableUserAuthState.addState(
                                    userState = TableUserAuthState(id, true)
                                ) ?: throw IOException(
                                    context.getString(
                                        R.string.couldn_t_update_signed_in_list_for_id,
                                        id
                                    )
                                )
                            }
                            .updateSharedPreferences {
                                putLong(CURRENT_USER_ID_KEY, id)
                            }
                    }
            }
        )
    }

    override suspend fun signOut() {
        withContext(defaultDispatcher) {
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
                    throw IOException(context.getString(R.string.no_user_with_id, id))
                }
            } else throw IOException(context.getString(R.string.no_signed_in_users))
        }
    }

    override suspend fun addUser(
        login: String,
        password: String,
        state: UserState
    ) = withContext(defaultDispatcher) {
        database.runInTransaction(
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
    }

    override suspend fun removeUser(id: Long) = withContext(defaultDispatcher) {
        tableUsersDao
            .removeUser(userId = id)
            .updateSharedPreferences {
                remove(CURRENT_USER_ID_KEY)
            }
    }

    override suspend fun getSignedIn(): Pair<List<UserInfo>, Long> = withContext(defaultDispatcher) {
        database.runInTransaction(
            body = Callable {
                tableUserAuthState.getAllSignedIn().map {
                    tableUserInfoDao.getInfoById(id = it)?.run {
                        UserInfo(id, state)
                    } ?: throw IOException(context.getString(R.string.caught_user_without_info_id, it))
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
    }

    override suspend fun setCurrent(id: Long) = withContext(defaultDispatcher) {
        tableUserAuthState.getAuthStateById(id)?.also {
            if (it) updateSharedPreferences {
                putLong(CURRENT_USER_ID_KEY, id)
            }
        }
    }

    private companion object {
        const val CURRENT_USER_ID_KEY = "current_user_key"
    }
}
