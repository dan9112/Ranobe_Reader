package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface ResultError : SignInError, RemoveAccountError, SignUpError {
    data object StorageError : ResultError
}
