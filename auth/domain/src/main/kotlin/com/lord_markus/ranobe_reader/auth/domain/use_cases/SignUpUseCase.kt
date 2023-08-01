package com.lord_markus.ranobe_reader.auth.domain.use_cases

import com.lord_markus.ranobe_reader.auth.domain.models.UserState
import com.lord_markus.ranobe_reader.auth.domain.repository.Repository

class SignUpUseCase(private val repository: Repository) {
    operator fun invoke(login: String, password: String, userState: UserState) =
        repository.signUp(login, password, userState)
}
