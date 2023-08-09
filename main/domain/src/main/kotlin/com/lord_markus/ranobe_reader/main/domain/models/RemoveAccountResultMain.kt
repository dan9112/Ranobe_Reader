package com.lord_markus.ranobe_reader.main.domain.models

import com.lord_markus.ranobe_reader.core.models.UserInfo

sealed interface RemoveAccountResultMain : MainUseCaseResult {
    data class Success(val users: List<UserInfo>) : RemoveAccountResultMain

    data class Error(val error: RemoveAccountError) : RemoveAccountResultMain
}
