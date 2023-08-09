package com.lord_markus.ranobe_reader.main.di

import com.lord_markus.ranobe_reader.main.domain.repository.MainRepository
import com.lord_markus.ranobe_reader.main.domain.use_cases.RemoveAccountUseCase
import com.lord_markus.ranobe_reader.main.domain.use_cases.SetCurrentUseCase
import com.lord_markus.ranobe_reader.main.domain.use_cases.SignOutUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
data object DomainModule {

    @Provides
    fun getRemoveAccountUseCase(repository: MainRepository) = RemoveAccountUseCase(repository)

    @Provides
    fun getSetCurrentUseCase(repository: MainRepository) = SetCurrentUseCase(repository)

    @Provides
    fun getSignOutUseCase(repository: MainRepository) = SignOutUseCase(repository)
}
