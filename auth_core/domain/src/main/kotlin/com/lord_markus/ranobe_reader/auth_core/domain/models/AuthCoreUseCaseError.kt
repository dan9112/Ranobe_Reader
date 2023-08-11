package com.lord_markus.ranobe_reader.auth_core.domain.models

sealed interface AuthCoreUseCaseError : SignInError, SignUpError {
    data class StorageError(val message: String?) : AuthCoreUseCaseError
}
