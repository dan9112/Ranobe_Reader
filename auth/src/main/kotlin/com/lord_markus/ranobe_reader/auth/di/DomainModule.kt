package com.lord_markus.ranobe_reader.auth.di

import com.lord_markus.ranobe_reader.auth.domain.repository.AuthRepository
import com.lord_markus.ranobe_reader.auth.domain.use_cases.GetSignedInUsersUseCase
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignUpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    fun getGetSignedInUsersUseCase(authRepository: AuthRepository) = GetSignedInUsersUseCase(authRepository)

    @Provides
    fun getSignInUseCase(authRepository: AuthRepository) = SignInUseCase(authRepository)

    @Provides
    fun getSignUpUseCase(authRepository: AuthRepository) = SignUpUseCase(authRepository)
}
