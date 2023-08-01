package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface SignUpResult {
    data object Success : SignUpResult
    data class Error(val error: SignUpError) : SignUpResult
}