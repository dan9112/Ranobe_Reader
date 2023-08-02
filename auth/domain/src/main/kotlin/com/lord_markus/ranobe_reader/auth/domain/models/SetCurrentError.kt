package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface SetCurrentError {
    data object UserNotSignedIn : SetCurrentError
    data object NoAuthInfo : SetCurrentError
}
