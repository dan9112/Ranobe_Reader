package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface AuthCheckResult {
    data class Success(val signedIn: Boolean) : AuthCheckResult

    data class Error(val error: AuthCheckError) : AuthCheckResult
}
