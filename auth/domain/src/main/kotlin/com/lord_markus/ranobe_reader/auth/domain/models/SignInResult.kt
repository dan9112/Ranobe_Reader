package com.lord_markus.ranobe_reader.auth.domain.models

import com.lord_markus.ranobe_reader.core.UserInfo

sealed interface SignInResult : UseCaseResult {
    data class Success(val userInfo: UserInfo) : SignInResult

    data class Error(val error: SignInError) : SignInResult
}
