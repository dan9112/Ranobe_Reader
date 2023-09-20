package com.lord_markus.ranobe_reader.settings.domain.use_cases

import com.lord_markus.ranobe_reader.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class SetNightModeUseCase @Inject constructor(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(flag: Boolean?) = settingsRepository.setNightMode(flag)
}
