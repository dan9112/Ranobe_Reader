package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface ResultError : SignInError, RemoveAccountError, SignUpError, SignOutError,
    AuthCheckError, SetCurrentError {
    data class StorageError(val message: String?) : ResultError
}
