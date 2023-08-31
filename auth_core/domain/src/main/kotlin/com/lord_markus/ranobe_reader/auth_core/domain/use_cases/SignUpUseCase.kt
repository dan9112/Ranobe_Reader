package com.lord_markus.ranobe_reader.auth_core.domain.use_cases

import com.lord_markus.ranobe_reader.auth_core.domain.repository.AuthCoreRepository
import com.lord_markus.ranobe_reader.core.models.UserState
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val authRepository: AuthCoreRepository) {
    suspend operator fun invoke(login: String, password: String, userState: UserState, withSignIn: Boolean) =
        authRepository.signUp(login, password, userState, withSignIn)
}
