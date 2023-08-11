package com.lord_markus.ranobe_reader.core.models

import kotlinx.serialization.Serializable

@Serializable
sealed interface UserState : java.io.Serializable {
    val number: Short

    @Serializable
    data object Admin : UserState {
        private fun readResolve(): Any = Admin
        override val number: Short = 0
    }

    @Serializable
    data object User : UserState {
        private fun readResolve(): Any = User
        override val number: Short = 1
    }
}
