package com.lord_markus.ranobe_reader.main.domain.use_cases

import com.lord_markus.ranobe_reader.main.domain.repository.MainRepository
import javax.inject.Inject

class SignOutWithRemoveUseCase @Inject constructor(private val mainRepository: MainRepository) {
    suspend operator fun invoke() = mainRepository.signOutWithRemove()
}
