package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface SignInResult {
    data class Success(val userInfo: UserState) : SignInResult

    data class Error(val error: SignInError) : SignInResult
}
