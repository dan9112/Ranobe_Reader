package com.lord_markus.ranobe_reader.auth_core.domain.models

import com.lord_markus.ranobe_reader.core.models.UserInfo

sealed interface SignInResultAuth : AuthUseCaseResult {
    data class Success(val userInfo: UserInfo) : SignInResultAuth

    data class Error(val error: SignInError) : SignInResultAuth
}
