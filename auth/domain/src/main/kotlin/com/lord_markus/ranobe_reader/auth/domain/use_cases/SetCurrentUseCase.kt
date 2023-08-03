package com.lord_markus.ranobe_reader.auth.domain.use_cases

import com.lord_markus.ranobe_reader.auth.domain.repository.Repository

class SetCurrentUseCase(private val repository: Repository) {
    suspend operator fun invoke(id: Long) = repository.setCurrent(id)
}
