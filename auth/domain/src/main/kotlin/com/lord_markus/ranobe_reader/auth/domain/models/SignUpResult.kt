package com.lord_markus.ranobe_reader.auth.domain.models

import com.lord_markus.ranobe_reader.core.models.UserInfo

sealed interface SignUpResult : UseCaseResult {
    data class Success(val userInfo: UserInfo) : SignUpResult
    data class Error(val error: SignUpError) : SignUpResult
}