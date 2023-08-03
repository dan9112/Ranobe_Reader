package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface SetCurrentResult : UseCaseResult {
    data object Success : SetCurrentResult
    data class Error(val error: SetCurrentError) : SetCurrentResult
}
