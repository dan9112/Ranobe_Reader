package com.lord_markus.ranobe_reader.auth.presentation.models

import com.lord_markus.ranobe_reader.auth.domain.models.UseCaseResult

sealed interface UseCaseState<in UseCaseResult> {
    data object Default : UseCaseState<UseCaseResult>
    data object InProcess : UseCaseState<UseCaseResult>
    data class ResulReceived<T>(val result: T) : UseCaseState<T>
}
