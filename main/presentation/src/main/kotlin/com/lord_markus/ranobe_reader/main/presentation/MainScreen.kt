package com.lord_markus.ranobe_reader.main.presentation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme
import com.lord_markus.ranobe_reader.main.domain.models.MainUseCaseError
import com.lord_markus.ranobe_reader.main.domain.models.SetCurrentResultMain
import com.lord_markus.ranobe_reader.main.domain.models.SignOutResultMain
import com.lord_markus.ranobe_reader.main.domain.repository.MainRepository
import com.lord_markus.ranobe_reader.main.domain.use_cases.SetCurrentUseCase
import com.lord_markus.ranobe_reader.main.domain.use_cases.SignOutUseCase
import com.lord_markus.ranobe_reader.main.domain.use_cases.SignOutWithRemoveUseCase
import com.lord_markus.ranobe_reader.main.presentation.models.ExtendedMainUseCaseState
import com.lord_markus.ranobe_reader.main.presentation.models.MainUseCaseState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier,
    onBackPressed: @Composable (() -> Unit) -> Unit,
    addUsers: () -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
    users: List<UserInfo>,
    currentId: Long,
    updateSignedIn: (List<UserInfo>, Long?) -> Unit
) = ConstraintLayout(modifier = modifier) {
    val (indicator, content) = createRefs()
    val progressBarVisible = rememberSaveable { mutableStateOf(false) }

    val coroutineScope = CoroutineScope(Dispatchers.Main)
    onBackPressed {
        coroutineScope.cancel()
    }

    Content(
        modifier = Modifier
            .constrainAs(content) {
                linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            },
        addUsers = addUsers,
        coroutineScope = coroutineScope,
        users = users,
        currentId = currentId,
        viewModel = viewModel,
        updateSignedIn = { users, currentId ->
            coroutineScope.cancel()
            updateSignedIn(users, currentId)
        },
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
    addUsers: () -> Unit,
    coroutineScope: CoroutineScope,
    users: List<UserInfo>,
    currentId: Long,
    viewModel: MainViewModel,
    updateSignedIn: (List<UserInfo>, Long?) -> Unit,
    switchIndicator: (Boolean) -> Unit// todo: добавить деактивацию всех интерактивных элементов при включении!
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.signedIn.collectLatest {
            when (val currentState = it) {
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
                                    context,
                                    when (val error = result.error) {
                                        is MainUseCaseError.StorageError -> error.message// todo: добавить корректную обработку
                                    },
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            is SignOutResultMain.Success -> {
                                val list = result.signedIn
                                Log.e("MyLog", list.joinToString())

                                updateSignedIn(list, if (list.isEmpty()) null else list.first().id)
                            }
                        }
                    }
                }
            }
        }
    }

    ConstraintLayout(modifier = modifier) {
        val (usersView, usersTitleView, mainTitleView, signInButton) = createRefs()

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
                Row(
                    modifier = if (user.id == currentId) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val textColor = MaterialTheme.colorScheme.run {
                        if (user.id == currentId) onPrimary else primary
                    }
                    Text(
                        text = user.id.toString(),
                        modifier = Modifier.weight(1f),
                        color = textColor
                    )// todo: заменить на имя пользователя!
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = user.state.toString(), modifier = Modifier.weight(5f), color = textColor)
                    Button(
                        onClick = {
                            if (user.id != currentId) {
                                val setCurrentStateCollector = FlowCollector<MainUseCaseState<SetCurrentResultMain>> {
                                    when (it) {
                                        MainUseCaseState.InProcess -> {
                                            switchIndicator(true)
                                        }

                                        is MainUseCaseState.ResultReceived -> {
                                            switchIndicator(false)
                                            when (it.result) {
                                                is SetCurrentResultMain.Error -> Toast.makeText(
                                                    context,
                                                    "Attempt to switch account failed!",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                SetCurrentResultMain.Success -> updateSignedIn(users, user.id)
                                            }
                                        }
                                    }
                                }
                                coroutineScope.launch {
                                    viewModel.current.collect(setCurrentStateCollector)
                                }
                                viewModel.setCurrent(user.id)
                            } else {
                                viewModel.signOut()
                            }
                        }
                    ) {
                        Text(text = if (user.id != currentId) "Switch" else "SignOut")
                    }
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
            onClick = addUsers,
            modifier = Modifier.constrainAs(signInButton) {
                linkTo(start = startGuideline, end = endGuideline)
                top.linkTo(anchor = usersView.bottom, margin = 8.dp)
            }
        ) {
            Text(text = "Add account")
        }
    }
}

@Preview(device = "spec:parent=Nexus 10")
@Composable
fun PreviewContent() = RanobeReaderTheme {
    Content(
        modifier = Modifier.fillMaxSize(),
        addUsers = { },
        coroutineScope = CoroutineScope(Dispatchers.Default),
        users = listOf(
            UserInfo(id = 0, state = UserState.Admin),
            UserInfo(id = 1, state = UserState.User),
            UserInfo(id = 2, state = UserState.User),
            UserInfo(id = 3, state = UserState.User),
            UserInfo(id = 4, state = UserState.User)
        ),
        currentId = 1,
        viewModel = viewModelStub,
        updateSignedIn = { _, _ -> },
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
        setCurrentUseCase = SetCurrentUseCase(mainRepositoryStub)
    )
}
