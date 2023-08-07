package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface SignInResult : UseCaseResult {
    data class Success(val userInfo: UserInfo) : SignInResult

    data class Error(val error: SignInError) : SignInResult
}
