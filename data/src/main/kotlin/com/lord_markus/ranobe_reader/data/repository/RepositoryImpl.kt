package com.lord_markus.ranobe_reader.data.repository

import com.lord_markus.ranobe_reader.app.domain.repository.AppRepository
import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth.domain.models.AuthUseCaseError
import com.lord_markus.ranobe_reader.auth.domain.repository.AuthRepository
import com.lord_markus.ranobe_reader.auth_core.domain.models.*
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState
import com.lord_markus.ranobe_reader.data.storage.template.db.IDataSource
import com.lord_markus.ranobe_reader.main.domain.models.MainUseCaseError
import com.lord_markus.ranobe_reader.main.domain.models.SetCurrentError
import com.lord_markus.ranobe_reader.main.domain.models.SetCurrentResultMain
import com.lord_markus.ranobe_reader.main.domain.models.SignOutResultMain
import com.lord_markus.ranobe_reader.main.domain.repository.MainRepository
import com.lord_markus.ranobe_reader.settings.domain.models.SettingsData
import com.lord_markus.ranobe_reader.settings.domain.repository.SettingsRepository
import java.io.IOException
import javax.inject.Inject

class RepositoryImpl @Inject constructor(private val dataSource: IDataSource) :
    AuthRepository, MainRepository, AppRepository, SettingsRepository {
    override suspend fun getSignedInUsers(): AuthCheckResult = try {
        dataSource.getSignedIn().let { signedInUsers ->
            signedInUsers?.run {
                AuthCheckResult.Success.SignedIn(signedIn = first, currentUserId = second)
            } ?: AuthCheckResult.Success.NoSuchUsers
        }
    } catch (e: IOException) {
        AuthCheckResult.Error(error = AuthUseCaseError.StorageError(message = e.message))
    }

    override suspend fun signIn(login: String, password: String, update: Boolean) = try {
        dataSource.signIn(login, password, update)?.run {
            SignInResultAuth.Success(userInfo = UserInfo(id = id, name = name, state = state))
        } ?: SignInResultAuth.Error(error = SignInError.NoSuchUser)
    } catch (e: IOException) {
        SignInResultAuth.Error(error = AuthCoreUseCaseError.StorageError(message = e.message))
    }

    override suspend fun signOut() = try {
        SignOutResultMain.Success(signedIn = dataSource.signOut())
    } catch (e: IOException) {
        SignOutResultMain.Error(error = MainUseCaseError.StorageError(message = e.message))
    }

    override suspend fun signUp(login: String, password: String, state: UserState, withSignIn: Boolean) = try {
        dataSource.addUser(login, password, state, withSignIn)?.let {
            SignUpResultAuth.Success(
                userInfo = UserInfo(id = it, name = login, state = state)
            )
        }
            ?: SignUpResultAuth.Error(error = SignUpError.LoginAlreadyInUse)
    } catch (e: IOException) {
        SignUpResultAuth.Error(error = AuthCoreUseCaseError.StorageError(message = e.message))
    }

    override suspend fun signOutWithRemove() = try {
        SignOutResultMain.Success(signedIn = dataSource.signOutWithRemove())
    } catch (e: IOException) {
        SignOutResultMain.Error(error = MainUseCaseError.StorageError(message = e.message))
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


    // todo:
    //  1. Заменить на контейнеры
    //  2. Добавить обработку ошибки доступа к памяти
    override val settingsDataFlow = dataSource.settingsDataFlow

    override suspend fun setSettings(settingsData: SettingsData) = dataSource.setSettings(settingsData)
    override suspend fun setDynamicColor(on: Boolean) = dataSource.setDynamicColor(on)

    override suspend fun setNightMode(flag: Boolean?) = dataSource.setNightMode(flag)
}
