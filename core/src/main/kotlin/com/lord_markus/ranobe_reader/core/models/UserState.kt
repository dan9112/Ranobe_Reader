package com.lord_markus.ranobe_reader.core.models

import java.io.Serializable

sealed interface UserState : Serializable {
    val number: Short

    data object Admin : UserState {
        private fun readResolve(): Any = Admin
        override val number: Short = 0
    }

    data object User : UserState {
        private fun readResolve(): Any = User
        override val number: Short = 1
    }
}
