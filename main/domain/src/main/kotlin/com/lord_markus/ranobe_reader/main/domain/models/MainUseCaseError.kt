package com.lord_markus.ranobe_reader.main.domain.models

sealed interface MainUseCaseError : RemoveAccountError, SignOutError, SetCurrentError {
    data class StorageError(val message: String?) : MainUseCaseError
}
