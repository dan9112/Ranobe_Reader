package com.lord_markus.ranobe_reader.settings.di

import com.lord_markus.ranobe_reader.settings.domain.repository.SettingsRepository
import com.lord_markus.ranobe_reader.settings.domain.use_cases.SetDynamicColorUseCase
import com.lord_markus.ranobe_reader.settings.domain.use_cases.SetNightModeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    fun setNightModeUseCase(settingsRepository: SettingsRepository) = SetNightModeUseCase(settingsRepository)

    @Provides
    fun setDynamicColorUseCase(settingsRepository: SettingsRepository) = SetDynamicColorUseCase(settingsRepository)
}
