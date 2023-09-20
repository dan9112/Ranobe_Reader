package com.lord_markus.ranobe_reader.data.storage.implementation

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState
import com.lord_markus.ranobe_reader.data.R
import com.lord_markus.ranobe_reader.data.storage.template.db.IAppDatabase
import com.lord_markus.ranobe_reader.data.storage.template.db.IDataSource
import com.lord_markus.ranobe_reader.data.storage.template.db.dao.ITableUserAuthStateDao
import com.lord_markus.ranobe_reader.data.storage.template.db.dao.ITableUserDao
import com.lord_markus.ranobe_reader.data.storage.template.db.dao.ITableUserInfoDao
import com.lord_markus.ranobe_reader.data.storage.template.db.entities.TableUser
import com.lord_markus.ranobe_reader.data.storage.template.db.entities.TableUserAuthState
import com.lord_markus.ranobe_reader.data.storage.template.db.entities.TableUserInfo
import com.lord_markus.ranobe_reader.settings.domain.models.SettingsData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.Callable
import javax.inject.Inject
import com.lord_markus.ranobe_reader.app.domain.models.SettingsData as AppSettingsData

class DataSource @Inject constructor(
    private val tableUsersDao: ITableUserDao,
    private val tableUserInfoDao: ITableUserInfoDao,
    private val tableUserAuthState: ITableUserAuthStateDao,
    private val database: IAppDatabase,
    private val context: Context,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) : IDataSource {
    private val Context.usersDataStore by preferencesDataStore(name = "users")
    private val usersSharedPreferences
        get() = context.getSharedPreferences("users", MODE_PRIVATE)

    @Suppress("CommitPrefEdits")
    private fun <T> T.updateSharedPreferences(
        dataChange: SharedPreferences.Editor.(T) -> SharedPreferences.Editor
    ) = also {
        usersSharedPreferences
            .edit()
            .dataChange(it)
            .apply()
    }

    override suspend fun signIn(login: String, password: String, update: Boolean) = withContext(defaultDispatcher) {
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
                                UserInfo(id = id, name = login, state = state)
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
                            .apply {
                                if (update) runBlocking {
                                    context.usersDataStore.edit { preferences ->
                                        preferences[CURRENT_USER_ID_KEY_] = id
                                    }
                                }
                            }
                    }
            }
        )
    }

    override suspend fun signOut() = doSomethingWithUserAndGetSignedInList(R.string.failed_to_sign_out) {
        tableUserAuthState.changeState(id = it, state = false)
    }

    private suspend fun doSomethingWithUserAndGetSignedInList(
        @StringRes stringResIfNotDone: Int,
        action: (Long) -> Int
    ) = withContext(defaultDispatcher) {
        database.runInTransaction(
            body = Callable {
                if (usersSharedPreferences.contains(CURRENT_USER_ID_KEY)) {
                    val id = usersSharedPreferences.getLong(CURRENT_USER_ID_KEY, 1L)
                    if (action(id) == 0) {
                        updateSharedPreferences {
                            remove(CURRENT_USER_ID_KEY)
                        }
                        throw IOException(context.getString(stringResIfNotDone, id))
                    } else {
                        tableUserAuthState.getAllSignedIn().map {
                            tableUserInfoDao.getInfoById(id = it)?.run {
                                UserInfo(it, name, state)
                            } ?: throw IOException(context.getString(R.string.caught_user_without_info_id, it))
                        }
                            .sortedBy { it.name }
                            .apply {
                                updateSharedPreferences {
                                    if (isNotEmpty()) {
                                        putLong(CURRENT_USER_ID_KEY, first().id)
                                    } else {
                                        remove(CURRENT_USER_ID_KEY)
                                    }
                                }
                            }
                    }
                } else throw IOException(context.getString(R.string.no_signed_in_users))
            }
        )
    }

    override suspend fun signOutWithRemove() = doSomethingWithUserAndGetSignedInList(R.string.no_user_with_id) {
        tableUsersDao.removeUser(userId = it)
    }

    override suspend fun addUser(
        login: String,
        password: String,
        state: UserState,
        withSignIn: Boolean
    ) = withContext(defaultDispatcher) {
        database.runInTransaction(
            body = Callable {
                tableUsersDao.addUser(user = TableUser(id = 0L, login, password)).let { id ->
                    if (id < 0) {
                        null
                    } else {
                        tableUserInfoDao.addInfo(userInfo = TableUserInfo(id = id, name = login, state = state))
                            ?: return@let null
                        tableUserAuthState.addState(userState = TableUserAuthState(id = id, authState = withSignIn))
                            ?: return@let null
                        id
                    }
                        ?.also {
                            if (withSignIn) {
                                runBlocking {
                                    context.usersDataStore.edit { preferences ->
                                        preferences[CURRENT_USER_ID_KEY_] = id
                                    }
                                }
                                updateSharedPreferences {
                                    putLong(CURRENT_USER_ID_KEY, id)
                                }
                            }
                        }
                }
            }
        )
    }

    override suspend fun getSignedIn() = withContext(defaultDispatcher) {
        database.runInTransaction(
            body = Callable {
                tableUserAuthState.getAllSignedIn().map {
                    tableUserInfoDao.getInfoById(id = it)?.run {
                        UserInfo(id, name, state)
                    } ?: throw IOException(context.getString(R.string.caught_user_without_info_id, it))
                }
                    .sortedBy { it.name }
                    .let { usersInfo ->
                        if (usersInfo.isEmpty()) {
                            null
                        } else usersInfo to if (usersSharedPreferences.contains(CURRENT_USER_ID_KEY)) {
                            usersSharedPreferences.getLong(CURRENT_USER_ID_KEY, -1L)
                        } else {// никогда не должен срабатывать, страховка!
                            usersInfo.first().id.also { id ->
                                runBlocking {
                                    context.usersDataStore.edit { preferences ->
                                        preferences[CURRENT_USER_ID_KEY_] = id
                                    }
                                }
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
            if (it) {
                runBlocking {
                    context.usersDataStore.edit { preferences ->
                        preferences[CURRENT_USER_ID_KEY_] = id
                    }
                }
                updateSharedPreferences {
                    putLong(CURRENT_USER_ID_KEY, id)
                }
            }
        }
    }

    private val Context.settingsDataStore by preferencesDataStore(name = "settings")

    override val settingsDataFlow = context.settingsDataStore.data.map { preferences ->
        AppSettingsData(
            nightMode = preferences[NIGHT_MODE_KEY],
            dynamicColor = preferences[DYNAMIC_COLOR_KEY] ?: true
        )
    }

    override suspend fun setSettings(settingsData: SettingsData) {
        withContext(defaultDispatcher) {
            context.settingsDataStore.edit { preferences ->
                with(settingsData) {
                    nightMode?.let {
                        preferences[NIGHT_MODE_KEY] = it
                    } ?: preferences.remove(NIGHT_MODE_KEY)
                    if (!dynamicColor) preferences[DYNAMIC_COLOR_KEY] = false
                    else preferences.remove(DYNAMIC_COLOR_KEY)
                }
            }
        }
    }

    override suspend fun setNightMode(flag: Boolean?) {
        withContext(defaultDispatcher) {
            context.settingsDataStore.edit { preferences ->
                flag?.let {
                    preferences[NIGHT_MODE_KEY] = it
                } ?: preferences.remove(NIGHT_MODE_KEY)
            }
        }
    }

    override suspend fun setDynamicColor(on: Boolean) {
        withContext(defaultDispatcher) {
            context.settingsDataStore.edit { preferences ->
                if (!on) preferences[DYNAMIC_COLOR_KEY] = false
                else preferences.remove(DYNAMIC_COLOR_KEY)
            }
        }
    }

    private companion object {
        const val CURRENT_USER_ID_KEY = "current_user_key"
        val CURRENT_USER_ID_KEY_ = longPreferencesKey("current_user_key")

        val NIGHT_MODE_KEY = booleanPreferencesKey("nightMode")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic")
    }
}
