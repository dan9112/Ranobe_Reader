package com.lord_markus.ranobe_reader.auth.di

import com.lord_markus.ranobe_reader.auth.presentation.AuthViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val presentationModule = module {
    viewModel {
        AuthViewModel(signInUseCase = get(), signUpUseCase = get())
    }
}
