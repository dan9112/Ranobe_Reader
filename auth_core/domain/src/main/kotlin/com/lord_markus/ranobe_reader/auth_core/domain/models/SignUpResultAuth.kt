package com.lord_markus.ranobe_reader.auth_core.domain.models

import com.lord_markus.ranobe_reader.core.models.UserInfo

sealed interface SignUpResultAuth : AuthUseCaseResult {
    data class Success(val userInfo: UserInfo) : SignUpResultAuth
    data class Error(val error: SignUpError, val trigger: Boolean = true) : SignUpResultAuth
}
