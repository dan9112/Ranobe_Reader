package com.lord_markus.ranobe_reader.auth.domain.repository

import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth_core.domain.repository.AuthCoreRepository

interface AuthRepository : AuthCoreRepository {
    suspend fun getSignedInUsers(): AuthCheckResult
}
