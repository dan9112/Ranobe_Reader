package com.lord_markus.ranobe_reader.auth_core.presentation.models

import android.os.Parcelable
import com.lord_markus.ranobe_reader.auth_core.domain.models.AuthUseCaseResult
import kotlinx.parcelize.Parcelize

sealed interface AuthUseCaseState<in T : AuthUseCaseResult> : ExtendedAuthUseCaseState<T> {
    @Parcelize
    data object InProcess : AuthUseCaseState<AuthUseCaseResult>

    @Parcelize
    data class ResultReceived<T : AuthUseCaseResult>(val result: T) : AuthUseCaseState<T> {
        override fun equals(other: Any?) = other is ResultReceived<*> && other.result == result
        override fun hashCode() = result.hashCode()
    }
}

sealed interface ExtendedAuthUseCaseState<in T : AuthUseCaseResult> : Parcelable {
    @Parcelize
    data object Default : ExtendedAuthUseCaseState<AuthUseCaseResult>
}
