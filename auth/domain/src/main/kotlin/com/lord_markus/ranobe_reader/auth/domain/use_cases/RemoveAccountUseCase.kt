package com.lord_markus.ranobe_reader.auth.domain.use_cases

import com.lord_markus.ranobe_reader.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RemoveAccountUseCase @Inject constructor(private val authRepository: AuthRepository) {
    suspend operator fun invoke(userId: Long) = authRepository.removeAccount(userId)
}
