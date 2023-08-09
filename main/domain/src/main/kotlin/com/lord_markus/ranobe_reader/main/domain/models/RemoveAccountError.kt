package com.lord_markus.ranobe_reader.main.domain.models

sealed interface RemoveAccountError {
    data object NoSuchUser : RemoveAccountError
}
