package com.lord_markus.ranobe_reader.auth_core.domain.use_cases

import com.lord_markus.ranobe_reader.auth_core.domain.repository.AuthCoreRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(private val authRepository: AuthCoreRepository) {
    suspend operator fun invoke(login: String, password: String) = authRepository.signIn(login, password)
}