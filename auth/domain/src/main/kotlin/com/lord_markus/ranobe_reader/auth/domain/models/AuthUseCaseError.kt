package com.lord_markus.ranobe_reader.auth.domain.models

interface AuthUseCaseError : AuthCheckError {
    data class StorageError(val message: String?) : AuthUseCaseError
}
