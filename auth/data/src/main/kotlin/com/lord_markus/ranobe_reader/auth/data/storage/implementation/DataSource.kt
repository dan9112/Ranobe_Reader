package com.lord_markus.ranobe_reader.auth.data.storage.implementation

import com.lord_markus.ranobe_reader.auth.data.storage.template.db.IDataSource
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.dao.ITableUsersDao
import com.lord_markus.ranobe_reader.auth.data.storage.template.db.entities.TableUser
import com.lord_markus.ranobe_reader.auth.domain.models.RemoveAccountError
import com.lord_markus.ranobe_reader.auth.domain.models.RemoveAccountResult
import com.lord_markus.ranobe_reader.auth.domain.models.ResultError
import com.lord_markus.ranobe_reader.auth.domain.models.SignInError
import com.lord_markus.ranobe_reader.auth.domain.models.SignInResult
import com.lord_markus.ranobe_reader.auth.domain.models.SignUpError
import com.lord_markus.ranobe_reader.auth.domain.models.SignUpResult
import com.lord_markus.ranobe_reader.auth.domain.models.UserState
import java.io.IOException

class DataSource(
    private val tableUsersDao: ITableUsersDao
) : IDataSource {
    override fun getUserInfo(login: String, password: String) = try {
        tableUsersDao.getUserInfoByLoginAndPassword(login, password)?.let {
            SignInResult.Success(userInfo = it)
        } ?: SignInResult.Error(error = SignInError.NoSuchUser)
    } catch (e: IOException) {
        SignInResult.Error(error = ResultError.StorageError)
    }

    override fun addUser(login: String, password: String, state: UserState) = try {
        tableUsersDao.addUser(
            TableUser(id = 0L, login, password, state)
        )?.let { SignUpResult.Success } ?: SignUpResult.Error(error = SignUpError.LoginAlreadyInUse)
    } catch (e: IOException) {
        SignUpResult.Error(error = ResultError.StorageError)
    }

    override fun removeUser(id: Long) = try {
        if (tableUsersDao.removeUser(userId = id) > 0) {
            RemoveAccountResult.Success
        } else {
            RemoveAccountResult.Error(error = RemoveAccountError.NoSuchUser)
        }
    } catch (e: IOException) {
        RemoveAccountResult.Error(error = ResultError.StorageError)
    }
}
