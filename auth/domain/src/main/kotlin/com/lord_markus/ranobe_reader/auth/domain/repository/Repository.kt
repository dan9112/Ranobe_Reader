package com.lord_markus.ranobe_reader.auth.domain.repository

import com.lord_markus.ranobe_reader.auth.domain.models.*
import com.lord_markus.ranobe_reader.core.UserState

interface Repository {
    suspend fun getSignedInUsers(): AuthCheckResult
    suspend fun signIn(login: String, password: String): SignInResult
    suspend fun signOut(): SignOutResult
    suspend fun signUp(login: String, password: String, state: UserState): SignUpResult
    suspend fun removeAccount(userId: Long): RemoveAccountResult
    suspend fun setCurrent(id: Long): SetCurrentResult
}
