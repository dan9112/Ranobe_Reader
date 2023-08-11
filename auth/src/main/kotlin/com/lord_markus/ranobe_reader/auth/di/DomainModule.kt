package com.lord_markus.ranobe_reader.auth.di

import com.lord_markus.ranobe_reader.auth.domain.repository.AuthRepository
import com.lord_markus.ranobe_reader.auth.domain.use_cases.GetSignedInUsersUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    fun getGetSignedInUsersUseCase(authRepository: AuthRepository) = GetSignedInUsersUseCase(authRepository)
}
