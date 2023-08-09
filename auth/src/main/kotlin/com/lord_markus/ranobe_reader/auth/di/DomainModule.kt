package com.lord_markus.ranobe_reader.auth.di

import com.lord_markus.ranobe_reader.auth.domain.repository.Repository
import com.lord_markus.ranobe_reader.auth.domain.use_cases.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    @Provides
    fun getGetSignedInUsersUseCase(repository: Repository) = GetSignedInUsersUseCase(repository)

    @Provides
    fun getRemoveAccountUseCase(repository: Repository) = RemoveAccountUseCase(repository)

    @Provides
    fun getSetCurrentUseCase(repository: Repository) = SetCurrentUseCase(repository)

    @Provides
    fun getSignInUseCase(repository: Repository) = SignInUseCase(repository)

    @Provides
    fun getSignOutUseCase(repository: Repository) = SignOutUseCase(repository)

    @Provides
    fun getSignUpUseCase(repository: Repository) = SignUpUseCase(repository)
}
