package com.lord_markus.ranobe_reader.auth.domain.models

import com.lord_markus.ranobe_reader.core.models.UserInfo

sealed interface AuthCheckResultAuth : AuthUseCaseResult {

    sealed interface Success : AuthCheckResultAuth {
        data class SignedIn(val signedIn: List<UserInfo>, val currentUserId: Long) : Success
        data object NoSuchUsers : Success {
            private fun readResolve(): Any = NoSuchUsers
        }

    }

    data class Error(val error: AuthCheckError) : AuthCheckResultAuth
}
