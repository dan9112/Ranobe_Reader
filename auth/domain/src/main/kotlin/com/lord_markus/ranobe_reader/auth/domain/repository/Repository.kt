package com.lord_markus.ranobe_reader.auth.domain.repository

import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth.domain.models.RemoveAccountResult
import com.lord_markus.ranobe_reader.auth.domain.models.SetCurrentResult
import com.lord_markus.ranobe_reader.auth.domain.models.SignInResult
import com.lord_markus.ranobe_reader.auth.domain.models.SignOutResult
import com.lord_markus.ranobe_reader.auth.domain.models.SignUpResult
import com.lord_markus.ranobe_reader.auth.domain.models.UserState

interface Repository {
    fun getSignedInUsers(): AuthCheckResult
    fun signIn(login: String, password: String): SignInResult

    fun signOut(): SignOutResult
    fun signUp(login: String, password: String, state: UserState): SignUpResult

    fun removeAccount(userId: Long): RemoveAccountResult
    fun setCurrent(id: Long): SetCurrentResult
}
