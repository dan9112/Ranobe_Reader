package com.lord_markus.ranobe_reader.auth.presentation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth_core.presentation.AuthCoreScreen
import com.lord_markus.ranobe_reader.auth_core.presentation.AuthCoreViewModel
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthUseCaseState
import com.lord_markus.ranobe_reader.core.models.UserInfo
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

    Content(
        modifier = Modifier.constrainAs(content) {
            linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
            height = Dimension.fillToConstraints
            width = Dimension.fillToConstraints
        },
        uiVisibleFlow = viewModel.uiVisibleFlow,
        viewModel = viewModel,
        onBackPressed = onBackPressed,
        onSuccess = { onSuccess(listOf(it), it.id) }
    )
    Indicator(
        show = viewModel.authProgressBarVisible,
        modifier = Modifier
            .constrainAs(indicator) {
                linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
            }
    )
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
    viewModel: AuthCoreViewModel,
    onBackPressed: @Composable (() -> Unit) -> Unit,
    onSuccess: (UserInfo) -> Unit
) {
    val uiVisibleState = uiVisibleFlow.collectAsStateWithLifecycle()

    if (uiVisibleState.value) AuthCoreScreen(
        viewModel = viewModel,
        modifier = modifier,
        onBackPressed = onBackPressed,
        onSuccess = onSuccess,
        primary = true
    )
}
