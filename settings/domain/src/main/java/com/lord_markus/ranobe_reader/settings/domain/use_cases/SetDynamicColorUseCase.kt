package com.lord_markus.ranobe_reader.settings.domain.use_cases

import com.lord_markus.ranobe_reader.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class SetDynamicColorUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(on: Boolean) = settingsRepository.setDynamicColor(on)
}
