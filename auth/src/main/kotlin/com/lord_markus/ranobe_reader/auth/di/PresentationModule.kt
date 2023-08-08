package com.lord_markus.ranobe_reader.auth.di

import androidx.lifecycle.SavedStateHandle
import com.lord_markus.ranobe_reader.auth.presentation.AuthViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val presentationModule = module {
    viewModel<AuthViewModel> {
        AuthViewModel(
            savedStateHandler = SavedStateHandle(),
            getSignedInUsersUseCase = get(),
            signInUseCase = get(),
            signUpUseCase = get()
        )
    }
}
