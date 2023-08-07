package com.lord_markus.ranobe_reader.auth.presentation.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface AuthScreenState : Parcelable {
    @Parcelize
    data object SignIn : AuthScreenState

    @Parcelize
    data object SignUp : AuthScreenState
}
