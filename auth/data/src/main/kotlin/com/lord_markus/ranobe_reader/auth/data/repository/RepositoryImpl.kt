package com.lord_markus.ranobe_reader.auth.data.repository

import com.lord_markus.ranobe_reader.auth.data.storage.template.db.IDataSource
import com.lord_markus.ranobe_reader.auth.domain.models.*
import com.lord_markus.ranobe_reader.auth.domain.repository.Repository
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState
import java.io.IOException
import javax.inject.Inject

class RepositoryImpl @Inject constructor(private val dataSource: IDataSource) : Repository {
    override suspend fun getSignedInUsers(): AuthCheckResult = try {
        dataSource.getSignedIn().let { signedInUsers ->
            signedInUsers?.run {
                AuthCheckResult.Success.SignedIn(signedIn = first, currentUserId = second)
            } ?: AuthCheckResult.Success.NoSuchUsers
        }
    } catch (e: IOException) {
        AuthCheckResult.Error(error = ResultError.StorageError(message = e.message))
    }

    override suspend fun signIn(login: String, password: String) = try {
        dataSource.signIn(login, password)?.run {
            SignInResult.Success(userInfo = UserInfo(id = id, state = state))
        } ?: SignInResult.Error(error = SignInError.NoSuchUser)
    } catch (e: IOException) {
        SignInResult.Error(error = ResultError.StorageError(message = e.message))
    }

    override suspend fun signOut() = try {
        dataSource.signOut()
        SignOutResult.Success
    } catch (e: IOException) {
        SignOutResult.Error(error = ResultError.StorageError(message = e.message))
    }

    override suspend fun signUp(login: String, password: String, state: UserState) = try {
        dataSource.addUser(login, password, state)?.let {
            SignUpResult.Success(
                userInfo = UserInfo(id = it, state = state)
            )
        }
            ?: SignUpResult.Error(error = SignUpError.LoginAlreadyInUse)
    } catch (e: IOException) {
        SignUpResult.Error(error = ResultError.StorageError(message = e.message))
    }

    override suspend fun removeAccount(userId: Long) = try {
        if (dataSource.removeUser(userId) > 0) {
            RemoveAccountResult.Success
        } else {
            RemoveAccountResult.Error(error = RemoveAccountError.NoSuchUser)
        }
    } catch (e: IOException) {
        RemoveAccountResult.Error(error = ResultError.StorageError(message = e.message))
    }

    override suspend fun setCurrent(id: Long) = try {
        when (dataSource.setCurrent(id)) {
            true -> SetCurrentResult.Success
            false -> SetCurrentResult.Error(error = SetCurrentError.UserNotSignedIn)
            null -> SetCurrentResult.Error(error = SetCurrentError.NoAuthInfo)
        }
    } catch (e: IOException) {
        SetCurrentResult.Error(error = ResultError.StorageError(message = e.message))
    }
}
