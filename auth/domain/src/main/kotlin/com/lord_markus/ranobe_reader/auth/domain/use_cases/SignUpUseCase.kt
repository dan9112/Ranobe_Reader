package com.lord_markus.ranobe_reader.auth.domain.use_cases

import com.lord_markus.ranobe_reader.auth.domain.repository.AuthRepository
import com.lord_markus.ranobe_reader.core.models.UserState
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(login: String, password: String, userState: UserState) =
        authRepository.signUp(login, password, userState)
}
