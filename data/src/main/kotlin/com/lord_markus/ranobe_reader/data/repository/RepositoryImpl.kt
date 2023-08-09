package com.lord_markus.ranobe_reader.data.repository

import com.lord_markus.ranobe_reader.auth.domain.models.*
import com.lord_markus.ranobe_reader.auth.domain.repository.AuthRepository
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState
import com.lord_markus.ranobe_reader.data.storage.template.db.IDataSource
import com.lord_markus.ranobe_reader.main.domain.models.*
import com.lord_markus.ranobe_reader.main.domain.repository.MainRepository
import java.io.IOException
import javax.inject.Inject

class RepositoryImpl @Inject constructor(private val dataSource: IDataSource) : AuthRepository, MainRepository {
    override suspend fun getSignedInUsers(): AuthCheckResultAuth = try {
        dataSource.getSignedIn().let { signedInUsers ->
            signedInUsers?.run {
                AuthCheckResultAuth.Success.SignedIn(signedIn = first, currentUserId = second)
            } ?: AuthCheckResultAuth.Success.NoSuchUsers
        }
    } catch (e: IOException) {
        AuthCheckResultAuth.Error(error = AuthUseCaseError.StorageError(message = e.message))
    }

    override suspend fun signIn(login: String, password: String) = try {
        dataSource.signIn(login, password)?.run {
            SignInResultAuth.Success(userInfo = UserInfo(id = id, state = state))
        } ?: SignInResultAuth.Error(error = SignInError.NoSuchUser)
    } catch (e: IOException) {
        SignInResultAuth.Error(error = AuthUseCaseError.StorageError(message = e.message))
    }

    override suspend fun signOut() = try {
        SignOutResultMain.Success(signedIn = dataSource.signOut())
    } catch (e: IOException) {
        SignOutResultMain.Error(error = MainUseCaseError.StorageError(message = e.message))
    }

    override suspend fun signUp(login: String, password: String, state: UserState) = try {
        dataSource.addUser(login, password, state)?.let {
            SignUpResultAuth.Success(
                userInfo = UserInfo(id = it, state = state)
            )
        }
            ?: SignUpResultAuth.Error(error = SignUpError.LoginAlreadyInUse)
    } catch (e: IOException) {
        SignUpResultAuth.Error(error = AuthUseCaseError.StorageError(message = e.message))
    }

    override suspend fun removeAccount(userId: Long) = try {
        if (dataSource.removeUser(userId) > 0) {
            RemoveAccountResultMain.Success
        } else {
            RemoveAccountResultMain.Error(error = RemoveAccountError.NoSuchUser)
        }
    } catch (e: IOException) {
        RemoveAccountResultMain.Error(error = MainUseCaseError.StorageError(message = e.message))
    }

    override suspend fun setCurrent(id: Long) = try {
        when (dataSource.setCurrent(id)) {
            true -> SetCurrentResultMain.Success
            false -> SetCurrentResultMain.Error(error = SetCurrentError.UserNotSignedIn)
            null -> SetCurrentResultMain.Error(error = SetCurrentError.NoAuthInfo)
        }
    } catch (e: IOException) {
        SetCurrentResultMain.Error(error = MainUseCaseError.StorageError(message = e.message))
    }
}
