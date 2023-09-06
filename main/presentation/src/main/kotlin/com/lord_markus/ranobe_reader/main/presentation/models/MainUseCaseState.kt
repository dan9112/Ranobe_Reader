package com.lord_markus.ranobe_reader.main.presentation.models

import com.lord_markus.ranobe_reader.main.domain.models.MainUseCaseResult
import kotlinx.parcelize.Parcelize

sealed interface MainUseCaseState<in T : MainUseCaseResult> : ExtendedMainUseCaseState<T> {
    @Parcelize
    data object InProcess : MainUseCaseState<MainUseCaseResult>

    @Parcelize
    data class ResultReceived<T : MainUseCaseResult>(val result: T) : MainUseCaseState<T> {
        override fun equals(other: Any?) = other is ResultReceived<*> && other.result == result
        override fun hashCode() = result.hashCode()
    }
}