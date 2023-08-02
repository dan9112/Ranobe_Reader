package com.lord_markus.ranobe_reader.auth.data.repository

import com.lord_markus.ranobe_reader.auth.data.storage.template.db.IDataSource
import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth.domain.models.RemoveAccountError
import com.lord_markus.ranobe_reader.auth.domain.models.RemoveAccountResult
import com.lord_markus.ranobe_reader.auth.domain.models.ResultError
import com.lord_markus.ranobe_reader.auth.domain.models.SetCurrentError
import com.lord_markus.ranobe_reader.auth.domain.models.SetCurrentResult
import com.lord_markus.ranobe_reader.auth.domain.models.SignInError
import com.lord_markus.ranobe_reader.auth.domain.models.SignInResult
import com.lord_markus.ranobe_reader.auth.domain.models.SignOutResult
import com.lord_markus.ranobe_reader.auth.domain.models.SignUpError
import com.lord_markus.ranobe_reader.auth.domain.models.SignUpResult
import com.lord_markus.ranobe_reader.auth.domain.models.UserState
import com.lord_markus.ranobe_reader.auth.domain.repository.Repository
import java.io.IOException

class RepositoryImpl(
    private val dataSource: IDataSource
) : Repository {
    override fun getSignedInUsers(): AuthCheckResult = try {
        dataSource.getSignedIn().run {
            AuthCheckResult.Success(signedIn = first, currentUserId = second)
        }
    } catch (e: IOException) {
        AuthCheckResult.Error(error = ResultError.StorageError(message = e.message))
    }

    override fun signIn(login: String, password: String) = try {
        dataSource.signIn(login, password)?.run {
            SignInResult.Success(userInfo = state)
        } ?: SignInResult.Error(error = SignInError.NoSuchUser)
    } catch (e: IOException) {
        SignInResult.Error(error = ResultError.StorageError(message = e.message))
    }

    override fun signOut() = try {
        dataSource.signOut()
        SignOutResult.Success
    } catch (e: IOException) {
        SignOutResult.Error(error = ResultError.StorageError(message = e.message))
    }

    override fun signUp(login: String, password: String, state: UserState) = try {
        dataSource.addUser(login, password, state)?.run { SignUpResult.Success }
            ?: SignUpResult.Error(error = SignUpError.LoginAlreadyInUse)
    } catch (e: IOException) {
        SignUpResult.Error(error = ResultError.StorageError(message = e.message))
    }

    override fun removeAccount(userId: Long) = try {
        if (dataSource.removeUser(userId) > 0) {
            RemoveAccountResult.Success
        } else {
            RemoveAccountResult.Error(error = RemoveAccountError.NoSuchUser)
        }
    } catch (e: IOException) {
        RemoveAccountResult.Error(error = ResultError.StorageError(message = e.message))
    }

    override fun setCurrent(id: Long) = try {
        when (dataSource.setCurrent(id)) {
            true -> SetCurrentResult.Success
            false -> SetCurrentResult.Error(error = SetCurrentError.UserNotSignedIn)
            null -> SetCurrentResult.Error(error = SetCurrentError.NoAuthInfo)
        }
    } catch (e: IOException) {
        SetCurrentResult.Error(error = ResultError.StorageError(message = e.message))
    }
}
