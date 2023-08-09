package com.lord_markus.ranobe_reader.auth.presentation.models

import android.os.Parcelable
import com.lord_markus.ranobe_reader.auth.domain.models.AuthUseCaseResult
import kotlinx.parcelize.Parcelize

sealed interface AuthUseCaseState<in T : AuthUseCaseResult> : ExtendedAuthUseCaseState<T> {
    @Parcelize
    data object InProcess : AuthUseCaseState<AuthUseCaseResult>

    @Parcelize
    data class ResultReceived<T : AuthUseCaseResult>(val result: T, var trigger: Boolean = true) : AuthUseCaseState<T> {
        override fun equals(other: Any?) = other is ResultReceived<*> && other.result == result
        override fun hashCode() = result.hashCode()
    }
}

sealed interface ExtendedAuthUseCaseState<in T : AuthUseCaseResult> : Parcelable {
    @Parcelize
    data object Default : ExtendedAuthUseCaseState<AuthUseCaseResult>
}
