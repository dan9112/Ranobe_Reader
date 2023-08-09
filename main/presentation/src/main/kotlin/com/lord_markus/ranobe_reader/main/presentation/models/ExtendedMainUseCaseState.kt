package com.lord_markus.ranobe_reader.main.presentation.models

import android.os.Parcelable
import com.lord_markus.ranobe_reader.main.domain.models.MainUseCaseResult
import kotlinx.parcelize.Parcelize

sealed interface ExtendedMainUseCaseState<in T : MainUseCaseResult> : Parcelable {
    @Parcelize
    data object Default : ExtendedMainUseCaseState<MainUseCaseResult>
}