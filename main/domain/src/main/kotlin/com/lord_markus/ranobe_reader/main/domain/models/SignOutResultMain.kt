package com.lord_markus.ranobe_reader.main.domain.models

import com.lord_markus.ranobe_reader.core.models.UserInfo

sealed interface SignOutResultMain : MainUseCaseResult {
    data class Success(val signedIn: List<UserInfo>) : SignOutResultMain

    data class Error(val error: SignOutError) : SignOutResultMain
}
