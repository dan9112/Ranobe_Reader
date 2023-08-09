package com.lord_markus.ranobe_reader.main.domain.use_cases

import com.lord_markus.ranobe_reader.main.domain.repository.MainRepository
import javax.inject.Inject

class RemoveAccountUseCase @Inject constructor(private val mainRepository: MainRepository) {
    suspend operator fun invoke(userId: Long) = mainRepository.removeAccount(userId)
}
