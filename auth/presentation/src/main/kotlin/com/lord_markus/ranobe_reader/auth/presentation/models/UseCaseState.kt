package com.lord_markus.ranobe_reader.auth.presentation.models

import android.os.Parcelable
import com.lord_markus.ranobe_reader.auth.domain.models.UseCaseResult
import kotlinx.parcelize.Parcelize

sealed interface UseCaseState<in T : UseCaseResult> : ExtendedUseCaseState<T> {
    @Parcelize
    data object InProcess : UseCaseState<UseCaseResult>

    @Parcelize
    data class ResultReceived<T : UseCaseResult>(val result: T, var trigger: Boolean = true) : UseCaseState<T> {
        override fun equals(other: Any?) = other is ResultReceived<*> && other.result == result
        override fun hashCode() = result.hashCode()
    }
}

sealed interface ExtendedUseCaseState<in T : UseCaseResult> : Parcelable {
    @Parcelize
    data object Default : ExtendedUseCaseState<UseCaseResult>
}
