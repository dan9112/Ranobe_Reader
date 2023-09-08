package com.lord_markus.ranobe_reader.auth_core.presentation

import com.lord_markus.ranobe_reader.auth_core.domain.models.SignInResultAuth
import com.lord_markus.ranobe_reader.auth_core.domain.models.SignUpResultAuth
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthScreenState
import com.lord_markus.ranobe_reader.auth_core.presentation.models.ExtendedAuthUseCaseState
import kotlinx.coroutines.flow.StateFlow

data class AuthCoreScreenData(
    val authScreenFlow: StateFlow<AuthScreenState>,
    val switchAuthScreenState: () -> Unit,
    val signInState: StateFlow<ExtendedAuthUseCaseState<SignInResultAuth>>,
    val signUpState: StateFlow<ExtendedAuthUseCaseState<SignUpResultAuth>>,
    val trySignIn: (login: String, password: String, update: Boolean) -> Unit,
    val trySignUp: (login: String, password: String, password2: String) -> Unit,
    val resetSignInTrigger: () -> Unit,
    val resetSignUpTrigger: () -> Unit,
    val switchAuthCoreProgressBar: (Boolean) -> Unit,
    val indicatorShowFlow: StateFlow<Boolean>,
)
