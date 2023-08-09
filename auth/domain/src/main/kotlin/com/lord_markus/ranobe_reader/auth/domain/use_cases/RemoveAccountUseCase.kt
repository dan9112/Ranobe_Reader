package com.lord_markus.ranobe_reader.auth.domain.use_cases

import com.lord_markus.ranobe_reader.auth.domain.repository.Repository
import javax.inject.Inject

class RemoveAccountUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(userId: Long) = repository.removeAccount(userId)
}
