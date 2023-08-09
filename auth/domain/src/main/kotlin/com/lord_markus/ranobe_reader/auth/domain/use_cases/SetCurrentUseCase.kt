package com.lord_markus.ranobe_reader.auth.domain.use_cases

import com.lord_markus.ranobe_reader.auth.domain.repository.Repository
import javax.inject.Inject

class SetCurrentUseCase @Inject constructor(private val repository: Repository) {
    suspend operator fun invoke(id: Long) = repository.setCurrent(id)
}
