package com.lord_markus.ranobe_reader.main.domain.models

sealed interface RemoveAccountResultMain : MainUseCaseResult {
    data object Success : RemoveAccountResultMain {
        private fun readResolve(): Any = Success
    }

    data class Error(val error: RemoveAccountError) : RemoveAccountResultMain
}
