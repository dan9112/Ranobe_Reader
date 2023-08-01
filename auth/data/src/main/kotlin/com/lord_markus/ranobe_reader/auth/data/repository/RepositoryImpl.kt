package com.lord_markus.ranobe_reader.auth.data.repository

import com.lord_markus.ranobe_reader.auth.data.storage.template.db.IDataSource
import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth.domain.models.RemoveAccountError
import com.lord_markus.ranobe_reader.auth.domain.models.RemoveAccountResult
import com.lord_markus.ranobe_reader.auth.domain.models.ResultError
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
    private var currentUser: Long? = null// todo: заменить в дальнейшем!
    override fun checkAuthState() = AuthCheckResult.Success(signedIn = currentUser != null)

    override fun signIn(login: String, password: String) = try {
        dataSource.getUserInfo(login, password)?.let {
            currentUser = it.id// todo: заменить в дальнейшем!
            SignInResult.Success(userInfo = it.state)
        } ?: SignInResult.Error(error = SignInError.NoSuchUser)
    } catch (e: IOException) {
        SignInResult.Error(error = ResultError.StorageError)
    }

    override fun signOut() = SignOutResult.Success.apply {// todo: заменить в дальнейшем!
        currentUser = null
    }

    override fun signUp(login: String, password: String, state: UserState) = try {
        dataSource.addUser(login, password, state)?.let { SignUpResult.Success }
            ?: SignUpResult.Error(error = SignUpError.LoginAlreadyInUse)
    } catch (e: IOException) {
        SignUpResult.Error(error = ResultError.StorageError)
    }

    override fun removeAccount(userId: Long) = try {
        if (dataSource.removeUser(userId) > 0) {
            RemoveAccountResult.Success
        } else {
            RemoveAccountResult.Error(error = RemoveAccountError.NoSuchUser)
        }
    } catch (e: IOException) {
        RemoveAccountResult.Error(error = ResultError.StorageError)
    }
}
