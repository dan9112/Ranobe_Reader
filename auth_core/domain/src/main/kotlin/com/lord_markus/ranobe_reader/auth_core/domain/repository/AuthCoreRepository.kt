package com.lord_markus.ranobe_reader.auth_core.domain.repository

import com.lord_markus.ranobe_reader.auth_core.domain.models.SignInResultAuth
import com.lord_markus.ranobe_reader.auth_core.domain.models.SignUpResultAuth
import com.lord_markus.ranobe_reader.core.models.UserState

interface AuthCoreRepository {
    suspend fun signIn(login: String, password: String): SignInResultAuth
    suspend fun signUp(login: String, password: String, state: UserState): SignUpResultAuth
}