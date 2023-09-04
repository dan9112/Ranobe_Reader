package com.lord_markus.ranobe_reader.auth.presentation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth_core.presentation.AuthCoreScreen
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthUseCaseState
import com.lord_markus.ranobe_reader.core.models.UserInfo
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier,
    onBackPressed: @Composable (() -> Unit) -> Unit,
    onSuccess: (List<UserInfo>, Long) -> Unit
) = ConstraintLayout(modifier = modifier) {
    val (indicator, content) = createRefs()
    val progressBarVisible = remember { mutableStateOf(true) }

    val uiState = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.authState.collectLatest {
            when (val currentState = it) {
                AuthUseCaseState.InProcess -> {
                    progressBarVisible.value = true
                }

                is AuthUseCaseState.ResultReceived -> {
                    progressBarVisible.value = false
                    when (val result = currentState.result) {
                        is AuthCheckResult.Error -> TODO(reason = "Показать ошибку и закрыть приложение")

                        is AuthCheckResult.Success.NoSuchUsers -> {
                            uiState.value = true
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

    if (uiState.value) AuthCoreScreen(
        modifier = Modifier.constrainAs(content) {
            linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
            height = Dimension.fillToConstraints
            width = Dimension.fillToConstraints
        },
        showIndicator = { progressBarVisible.value = it },
        onBackPressed = onBackPressed,
        onSuccess = {
            uiState.value = false
            onSuccess(listOf(it), it.id)
        },
        primary = true
    )

    if (progressBarVisible.value) CircularProgressIndicator(
        modifier = Modifier
            .constrainAs(indicator) {
                linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
            }
    )
}
