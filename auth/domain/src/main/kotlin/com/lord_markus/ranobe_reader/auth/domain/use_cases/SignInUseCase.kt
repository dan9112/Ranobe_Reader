package com.lord_markus.ranobe_reader.auth.domain.use_cases

import com.lord_markus.ranobe_reader.auth.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(login: String, password: String) = authRepository.signIn(login, password)
}