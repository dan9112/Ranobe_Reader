package com.lord_markus.ranobe_reader.auth.presentation.models

import android.os.Parcelable
import com.lord_markus.ranobe_reader.auth.domain.models.AuthUseCaseResult
import kotlinx.parcelize.Parcelize

sealed interface UseCaseState<in T : AuthUseCaseResult> : ExtendedUseCaseState<T> {
    @Parcelize
    data object InProcess : UseCaseState<AuthUseCaseResult>

    @Parcelize
    data class ResultReceived<T : AuthUseCaseResult>(val result: T, var trigger: Boolean = true) : UseCaseState<T> {
        override fun equals(other: Any?) = other is ResultReceived<*> && other.result == result
        override fun hashCode() = result.hashCode()
    }
}

sealed interface ExtendedUseCaseState<in T : AuthUseCaseResult> : Parcelable {
    @Parcelize
    data object Default : ExtendedUseCaseState<AuthUseCaseResult>
}
