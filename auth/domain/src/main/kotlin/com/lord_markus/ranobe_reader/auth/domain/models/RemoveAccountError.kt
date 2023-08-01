package com.lord_markus.ranobe_reader.auth.domain.models

sealed interface RemoveAccountError {
    data object NoSuchUser : RemoveAccountError
}
