package com.lord_markus.ranobe_reader.main.domain.models

sealed interface SetCurrentError {
    data object UserNotSignedIn : SetCurrentError
    data object NoAuthInfo : SetCurrentError
}
