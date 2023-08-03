package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface RemoveAccountResult {
    data object Success : RemoveAccountResult

    data class Error(val error: RemoveAccountError) : RemoveAccountResult
}