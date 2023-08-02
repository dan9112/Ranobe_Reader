package com.lord_markus.ranobe_reader.auth.domain.use_cases

import com.lord_markus.ranobe_reader.auth.domain.repository.Repository

class GetSignedInUsersUseCase(private val repository: Repository) {
    operator fun invoke() = repository.getSignedInUsers()
}
