package com.lord_markus.ranobe_reader.auth.di

import com.lord_markus.ranobe_reader.auth.domain.use_cases.*
import org.koin.dsl.module

val domainModule = module {
    factory {
        GetSignedInUsersUseCase(repository = get())
    }
    factory {
        RemoveAccountUseCase(repository = get())
    }
    factory {
        SetCurrentUseCase(repository = get())
    }
    factory {
        SignInUseCase(repository = get())
    }
    factory {
        SignOutUseCase(repository = get())
    }
    factory {
        SignUpUseCase(repository = get())
    }
}
