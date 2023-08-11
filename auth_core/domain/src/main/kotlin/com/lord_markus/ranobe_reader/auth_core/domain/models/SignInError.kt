package com.lord_markus.ranobe_reader.auth_core.domain.models

sealed interface SignInError {
    data object NoSuchUser : SignInError
    data object IncorrectInput : SignInError
}
