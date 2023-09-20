package com.lord_markus.ranobe_reader.app.domain.use_cases

import com.lord_markus.ranobe_reader.app.domain.repository.AppRepository
import javax.inject.Inject

class GetSettingsDataUseCase @Inject constructor(private val appRepository: AppRepository) {
    operator fun invoke() = appRepository.settingsDataFlow
}
