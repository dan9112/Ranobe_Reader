package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface SignInError {
    data object NoSuchUser : SignInError
    data object IncorrectInput : SignInError
}
