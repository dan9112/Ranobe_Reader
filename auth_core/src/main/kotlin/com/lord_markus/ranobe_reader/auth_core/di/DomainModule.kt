package com.lord_markus.ranobe_reader.auth_core.di

import com.lord_markus.ranobe_reader.auth_core.domain.repository.AuthCoreRepository
import com.lord_markus.ranobe_reader.auth_core.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth_core.domain.use_cases.SignUpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    fun getSignInUseCase(authRepository: AuthCoreRepository) = SignInUseCase(authRepository)

    @Provides
    fun getSignUpUseCase(authRepository: AuthCoreRepository) = SignUpUseCase(authRepository)
}
