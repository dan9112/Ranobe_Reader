package com.lord_markus.ranobe_reader.main.domain.models

sealed interface SetCurrentResultMain : MainUseCaseResult {
    data object Success : SetCurrentResultMain {
        private fun readResolve(): Any = Success
    }

    data class Error(val error: SetCurrentError) : SetCurrentResultMain
}
