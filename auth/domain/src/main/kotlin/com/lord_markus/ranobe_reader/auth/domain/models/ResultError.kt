package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface ResultError : SignInError, AuthCheckError, RemoveAccountError, SignUpError,
    SignOutError {
    data object StorageError : ResultError
}
