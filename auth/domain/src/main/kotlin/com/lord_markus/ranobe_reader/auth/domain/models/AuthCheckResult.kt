package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface AuthCheckResult : UseCaseResult {
    data class Success(val signedIn: List<UserInfo>, val currentUserId: Long) : AuthCheckResult

    data class Error(val error: AuthCheckError) : AuthCheckResult
}
