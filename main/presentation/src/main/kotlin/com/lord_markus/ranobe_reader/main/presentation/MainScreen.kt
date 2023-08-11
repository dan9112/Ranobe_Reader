package com.lord_markus.ranobe_reader.main.presentation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme
import com.lord_markus.ranobe_reader.main.domain.models.MainUseCaseError
import com.lord_markus.ranobe_reader.main.domain.models.SetCurrentResultMain
import com.lord_markus.ranobe_reader.main.domain.models.SignOutResultMain
import com.lord_markus.ranobe_reader.main.domain.repository.MainRepository
import com.lord_markus.ranobe_reader.main.domain.use_cases.SignOutUseCase
import com.lord_markus.ranobe_reader.main.domain.use_cases.SignOutWithRemoveUseCase
import com.lord_markus.ranobe_reader.main.presentation.models.ExtendedMainUseCaseState
import com.lord_markus.ranobe_reader.main.presentation.models.MainUseCaseState

@Composable
fun MainScreen(
    modifier: Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    users: List<UserInfo>,
    updateSignedIn: (List<UserInfo>) -> Unit
) = ConstraintLayout(modifier = modifier) {
    val (indicator, content) = createRefs()
    val progressBarVisible = rememberSaveable { mutableStateOf(false) }

    Content(
        modifier = Modifier
            .constrainAs(content) {
                linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            },
        users = users,
        viewModel = viewModel,
        updateSignedIn = updateSignedIn,
        switchIndicator = { progressBarVisible.value = it }
    )

    if (progressBarVisible.value) CircularProgressIndicator(
        modifier = Modifier
            .constrainAs(indicator) {
                linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
            }
    )
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    users: List<UserInfo>,
    viewModel: MainViewModel,
    updateSignedIn: (List<UserInfo>) -> Unit,
    switchIndicator: (Boolean) -> Unit
) {
    val signedInState by viewModel.signedIn.collectAsStateWithLifecycle()
    when (val currentState = signedInState) {
        ExtendedMainUseCaseState.Default -> {
            switchIndicator(false)
        }

        MainUseCaseState.InProcess -> {
            switchIndicator(true)
        }

        is MainUseCaseState.ResultReceived -> {
            switchIndicator(false)
            if (currentState.trigger) {
                viewModel.caughtTrigger()
                when (val result = currentState.result) {
                    is SignOutResultMain.Error -> {
                        Toast.makeText(
                            LocalContext.current,
                            when (val error = result.error) {
                                is MainUseCaseError.StorageError -> error.message// todo: добавить корректную обработку
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is SignOutResultMain.Success -> {
                        val list = result.signedIn
                        Log.e("MyLog", list.joinToString())

                        updateSignedIn(list)
                    }
                }
            }
        }
    }

    ConstraintLayout(modifier = modifier) {
        val (usersView, usersTitleView, mainTitleView, signOutButton) = createRefs()

        val topGuideline = createGuidelineFromTop(fraction = 0.4f)
        val bottomGuideline = createGuidelineFromBottom(fraction = 0.45f)
        val startGuideline = createGuidelineFromStart(fraction = 0.25f)
        val endGuideline = createGuidelineFromEnd(fraction = 0.25f)
        LazyColumn(
            modifier = Modifier.constrainAs(usersView) {
                linkTo(top = topGuideline, start = startGuideline, end = endGuideline, bottom = bottomGuideline)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }
        ) {
            items(users) { user ->
                Row {
                    Text(text = user.id.toString(), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = user.state.toString(), modifier = Modifier.weight(5f))
                }
            }
        }

        Text(
            text = "Signed in users",
            modifier = Modifier.constrainAs(usersTitleView) {
                linkTo(start = startGuideline, end = endGuideline)
                bottom.linkTo(anchor = usersView.top, margin = 8.dp)
            }
        )
        Text(
            text = "Welcome, user!",
            fontSize = 30.sp,
            modifier = Modifier.constrainAs(mainTitleView) {
                linkTo(start = startGuideline, end = endGuideline)
                bottom.linkTo(anchor = usersTitleView.top, margin = 8.dp)
            }
        )
        Button(
            onClick = { viewModel.signOut() },
            modifier = Modifier.constrainAs(signOutButton) {
                linkTo(start = startGuideline, end = endGuideline)
                top.linkTo(anchor = usersView.bottom, margin = 8.dp)
            }
        ) {
            Text(text = "Exit")
        }
    }
}

@Preview(device = "spec:parent=Nexus 10")
@Composable
fun PreviewContent() = RanobeReaderTheme {
    Content(
        modifier = Modifier.fillMaxSize(),
        users = listOf(UserInfo(id = 1, state = UserState.User)),
        viewModel = viewModelStub,
        updateSignedIn = { },
        switchIndicator = { }
    )
}

private val mainRepositoryStub by lazy {
    object : MainRepository {
        override suspend fun signOut() = SignOutResultMain.Success(signedIn = listOf(UserInfo(1, UserState.User)))
        override suspend fun signOutWithRemove() =
            SignOutResultMain.Success(signedIn = listOf(UserInfo(1, UserState.User)))

        override suspend fun setCurrent(id: Long) = SetCurrentResultMain.Success
    }
}

private val viewModelStub by lazy {
    MainViewModel(
        savedStateHandler = SavedStateHandle(),
        signOutUseCase = SignOutUseCase(mainRepositoryStub),
        signOutWithRemoveUseCase = SignOutWithRemoveUseCase(mainRepositoryStub),
//        setCurrentUseCase = SetCurrentUseCase(mainRepositoryStub)
    )
}
