package com.lord_markus.ranobe_reader.auth.domain.use_cases

import com.lord_markus.ranobe_reader.auth.domain.repository.Repository

class CheckAuthUseCase(private val repository: Repository) {
    operator fun invoke(login: String, password: String) =
        repository.checkAuthState(login, password)
}
