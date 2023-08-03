package com.lord_markus.ranobe_reader.auth.domain.use_cases

import com.lord_markus.ranobe_reader.auth.domain.repository.Repository

class RemoveAccountUseCase(private val repository: Repository) {
    suspend operator fun invoke(userId: Long) = repository.removeAccount(userId)
}
