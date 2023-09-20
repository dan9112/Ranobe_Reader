package com.lord_markus.ranobe_reader.app.di

import com.lord_markus.ranobe_reader.app.domain.repository.AppRepository
import com.lord_markus.ranobe_reader.app.domain.use_cases.GetSettingsDataUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    fun getDynamicColorUseCase(appRepository: AppRepository) = GetSettingsDataUseCase(appRepository)
}
