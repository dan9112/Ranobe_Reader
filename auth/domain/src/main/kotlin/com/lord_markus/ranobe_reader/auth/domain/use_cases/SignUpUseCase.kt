package com.lord_markus.ranobe_reader.auth.domain.use_cases

import com.lord_markus.ranobe_reader.auth.domain.repository.Repository
import com.lord_markus.ranobe_reader.core.UserState

class SignUpUseCase(private val repository: Repository) {
    suspend operator fun invoke(login: String, password: String, userState: UserState) =
        repository.signUp(login, password, userState)
}
