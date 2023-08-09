package com.lord_markus.ranobe_reader.auth.domain.repository

import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth.domain.models.SignInResultAuth
import com.lord_markus.ranobe_reader.auth.domain.models.SignUpResultAuth
import com.lord_markus.ranobe_reader.core.models.UserState

interface AuthRepository {
    suspend fun getSignedInUsers(): AuthCheckResult
    suspend fun signIn(login: String, password: String): SignInResultAuth
    suspend fun signUp(login: String, password: String, state: UserState): SignUpResultAuth
}
