package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface SignOutResult : UseCaseResult {
    data object Success : SignOutResult

    data class Error(val error: SignOutError) : SignOutResult
}
