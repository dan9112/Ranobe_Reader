package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface AuthUseCaseError : SignInError, SignUpError, AuthCheckError {
    data class StorageError(val message: String?) : AuthUseCaseError
}
