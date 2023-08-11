package com.lord_markus.ranobe_reader.auth_core.domain.models

sealed interface SignUpError {
    data object LoginAlreadyInUse : SignUpError
    data object
    PasswordRequirements : SignUpError

    data object IncorrectInput : SignUpError
}
