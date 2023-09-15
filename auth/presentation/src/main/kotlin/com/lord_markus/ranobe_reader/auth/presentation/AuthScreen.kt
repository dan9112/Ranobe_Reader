package com.lord_markus.ranobe_reader.auth.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth_core.presentation.AuthCoreScreen
import com.lord_markus.ranobe_reader.auth_core.presentation.AuthCoreScreenData
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthScreenState
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthUseCaseState
import com.lord_markus.ranobe_reader.auth_core.presentation.models.ExtendedAuthUseCaseState
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier,
    onBackPressed: @Composable (() -> Unit) -> Unit,
    onSuccess: (List<UserInfo>, Long) -> Unit
) = ConstraintLayout(modifier = modifier) {
    val (indicator, content) = createRefs()

    LaunchedEffect(Unit) {
        viewModel.authState.collectLatest {
            when (val currentState = it) {
                AuthUseCaseState.InProcess -> {
                    viewModel.switchAuthProgressBar(true)
                }

                is AuthUseCaseState.ResultReceived -> {
                    viewModel.switchAuthProgressBar(false)
                    when (val result = currentState.result) {
                        is AuthCheckResult.Error -> TODO(reason = "Показать ошибку и закрыть приложение")

                        is AuthCheckResult.Success.NoSuchUsers -> {
                            viewModel.switchUiVisible(true)
                        }

                        is AuthCheckResult.Success.SignedIn -> {
                            result.run {
                                onSuccess(signedIn, currentUserId)
                            }
                        }
                    }
                }
            }
        }
    }

    viewModel.run {
        Content(
            modifier = Modifier.constrainAs(content) {
                linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            },
            uiVisibleFlow = uiVisibleFlow,
            authCoreScreenData = AuthCoreScreenData(
                authScreenFlow = authScreenFlow,
                switchAuthScreenState = ::switchAuthScreenState,
                signInState = signInState,
                signUpState = signUpState,
                trySignIn = ::trySignIn,
                trySignUp = ::trySignUp,
                resetSignInTrigger = ::resetSignInTrigger,
                resetSignInState = ::resetSignInState,
                resetSignUpTrigger = ::resetSignUpTrigger,
                resetSignUpState = ::resetSignUpState,
                switchAuthCoreProgressBar = ::switchAuthCoreProgressBar,
                indicatorShowFlow = authCoreProgressBarVisible
            ),
            onBackPressed = onBackPressed,
            onSuccess = { onSuccess(listOf(it), it.id) }
        )
        Indicator(
            show = authProgressBarVisible,
            modifier = Modifier
                .constrainAs(indicator) {
                    linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
                }
        )
    }
}

@Composable
private fun Indicator(show: StateFlow<Boolean>, modifier: Modifier) {
    val showState = show.collectAsStateWithLifecycle()
    if (showState.value) CircularProgressIndicator(modifier = modifier)
}

@Composable
private fun Content(
    modifier: Modifier,
    uiVisibleFlow: StateFlow<Boolean>,
    authCoreScreenData: AuthCoreScreenData,
    onBackPressed: @Composable (() -> Unit) -> Unit,
    onSuccess: (UserInfo) -> Unit
) {
    val uiVisibleState = uiVisibleFlow.collectAsStateWithLifecycle()

    if (uiVisibleState.value) AuthCoreScreen(
        authCoreScreenData = authCoreScreenData,
        modifier = modifier,
        onBackPressed = onBackPressed,
        onSuccess = onSuccess,
        primary = true
    )
}

internal class AuthScreenPreviewParameterProvider : PreviewParameterProvider<AuthCoreScreenData> {
    private val authScreenStateSequence = sequenceOf(AuthScreenState.SignIn, AuthScreenState.SignUp)

    override val values = authScreenStateSequence
        .map { authScreenState ->
            AuthCoreScreenData(
                authScreenFlow = MutableStateFlow(authScreenState),
                switchAuthScreenState = {},
                signInState = MutableStateFlow(ExtendedAuthUseCaseState.Default),
                signUpState = MutableStateFlow(ExtendedAuthUseCaseState.Default),
                trySignIn = { _, _, _ -> },
                trySignUp = { _, _, _ -> },
                resetSignInTrigger = {},
                resetSignInState = {},
                resetSignUpTrigger = {},
                resetSignUpState = {},
                switchAuthCoreProgressBar = {},
                indicatorShowFlow = MutableStateFlow(false)
            )
        }
}

@Preview(device = "spec:parent=Nexus 10")
@Composable
internal fun ContentPreview(
    @PreviewParameter(AuthScreenPreviewParameterProvider::class) authCoreScreenData: AuthCoreScreenData
) {
    RanobeReaderTheme {
        Content(
            modifier = Modifier.fillMaxSize(),
            uiVisibleFlow = MutableStateFlow(true),
            authCoreScreenData = authCoreScreenData,
            onBackPressed = {},
            onSuccess = {}
        )
    }
}
