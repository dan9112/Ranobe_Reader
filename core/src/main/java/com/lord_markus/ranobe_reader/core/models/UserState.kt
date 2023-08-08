package com.lord_markus.ranobe_reader.core.models

import java.io.Serializable

sealed interface UserState : Serializable {
    data object Admin : UserState {
        private fun readResolve(): Any = Admin
    }

    data object User : UserState {
        private fun readResolve(): Any = User
    }
}
