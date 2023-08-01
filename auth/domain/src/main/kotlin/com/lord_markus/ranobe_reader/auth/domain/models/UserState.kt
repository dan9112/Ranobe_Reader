package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface UserState {
    val number: Short

    data object Admin : UserState {
        override val number: Short = 0
    }

    data object User : UserState {
        override val number: Short = 1
    }
}
