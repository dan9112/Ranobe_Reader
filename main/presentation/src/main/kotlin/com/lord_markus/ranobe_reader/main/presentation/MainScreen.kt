package com.lord_markus.ranobe_reader.main.presentation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lord_markus.ranobe_reader.auth_core.presentation.AuthCoreScreen
import com.lord_markus.ranobe_reader.auth_core.presentation.AuthCoreViewModel
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme
import com.lord_markus.ranobe_reader.main.domain.models.MainUseCaseError
import com.lord_markus.ranobe_reader.main.domain.models.SetCurrentResultMain
import com.lord_markus.ranobe_reader.main.domain.models.SignOutResultMain
import com.lord_markus.ranobe_reader.main.presentation.models.MainUseCaseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    users: List<UserInfo>,
    currentId: Long,
    updateSignedIn: (List<UserInfo>, Long?) -> Unit
) = ConstraintLayout(modifier = modifier) {
    Log.i("ComposeLog", "MainScreen")
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val (indicator, content) = createRefs()

    val currentIdTrigger: (Long) -> Unit = { id ->
        with(receiver = viewModel) {
            if (id != currentId) {
                coroutineScope.launch {
                    setCurrentFlow.collect {
                        when (it) {
                            MainUseCaseState.InProcess -> {
                                viewModel.switchProgressBar(true)
                            }

                            is MainUseCaseState.ResultReceived -> {
                                viewModel.switchProgressBar(false)
                                when (it.result) {
                                    is SetCurrentResultMain.Error -> Toast.makeText(
                                        context,
                                        "Attempt to switch account failed!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    SetCurrentResultMain.Success -> updateSignedIn(users, id)
                                }
                            }
                        }
                    }
                }
                setCurrent(id)
            } else {
                coroutineScope.launch {
                    signOutFlow.collect {
                        when (val currentState = it) {
                            MainUseCaseState.InProcess -> {
                                viewModel.switchProgressBar(true)
                            }

                            is MainUseCaseState.ResultReceived -> {
                                viewModel.switchProgressBar(false)
                                when (val result = currentState.result) {
                                    is SignOutResultMain.Error -> {
                                        if (result.trigger) {
                                            caughtTrigger()
                                            Toast.makeText(
                                                context,
                                                when (val error = result.error) {
                                                    is MainUseCaseError.StorageError -> error.message// todo: добавить корректную обработку
                                                },
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }

                                    is SignOutResultMain.Success -> {
                                        val list = result.signedIn
                                        Log.i("MyLog", list.joinToString())

                                        updateSignedIn(
                                            list,
                                            if (list.isEmpty()) null else list.first().id
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                signOut()
            }
        }
    }

    AuthDialog(
        show = viewModel.dialogInUse,
        viewModel = viewModel,
        resetAuthCoreViewModel = viewModel::resetAuthCoreViewModel,
        onSuccess = { user ->
            updateSignedIn((users + user).sortedBy { it.id }, user.id)
        },
        onDismiss = {
            Log.i("MyLog", "Dialog dismissed")
            viewModel.switchDialog(false)
        }
    )

    Content(
        modifier = Modifier
            .constrainAs(content) {
                linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            },
        currentId = currentId,
        users = users,
        showDialog = { viewModel.switchDialog(true) },
        currentIdTrigger = currentIdTrigger,
        disable = viewModel.progressBarVisible
    )

    Indicator(
        show = viewModel.progressBarVisible,
        modifier = Modifier
            .constrainAs(indicator) {
                linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
            }
    )
}

@Composable
private fun Indicator(show: StateFlow<Boolean>, modifier: Modifier) {
    val showState = show.collectAsStateWithLifecycle()
    Log.i("ComposeLog", "Indicator - ${showState.value}")
    if (showState.value) CircularProgressIndicator(modifier = modifier)
}

@Composable
private fun AuthDialog(
    show: StateFlow<Boolean>,
    viewModel: AuthCoreViewModel,
    resetAuthCoreViewModel: () -> Unit,
    onSuccess: (UserInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val showState = show.collectAsStateWithLifecycle()
    Log.i("ComposeLog", "AuthDialog - ${showState.value}")
    if (showState.value) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                AuthCoreScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    viewModel = viewModel,
                    onBackPressed = { onDismiss() },
                    onSuccess = onSuccess,
                    primary = false
                )
            }
        }
    } else {
        resetAuthCoreViewModel()
    }
}

@Composable
private fun Content(
    modifier: Modifier = Modifier,
    currentId: Long,
    users: List<UserInfo>,
    showDialog: () -> Unit,
    currentIdTrigger: (Long) -> Unit,
    disable: StateFlow<Boolean>
) {
    val disableState = disable.collectAsStateWithLifecycle()
    Log.i("ComposeLog", "Content")
    Log.i("MyLog", "Current user list:\n${users.joinToString()}")

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
            },
            userScrollEnabled = !disableState.value
        ) {
            items(users) { user ->
                AccountRow(
                    buttonTrigger = { currentIdTrigger(user.id) },
                    user = user,
                    currentUser = user.id == currentId,
                    disableState = disableState
                )
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
            text = "Welcome, ${users.find { it.id == currentId }?.name}!",
            fontSize = 30.sp,
            modifier = Modifier.constrainAs(mainTitleView) {
                linkTo(start = startGuideline, end = endGuideline)
                bottom.linkTo(anchor = usersTitleView.top, margin = 8.dp)
            }
        )
        Button(
            modifier = Modifier.constrainAs(signInButton) {
                linkTo(start = startGuideline, end = endGuideline)
                top.linkTo(anchor = usersView.bottom, margin = 8.dp)
            },
            onClick = showDialog,
            enabled = !disableState.value
        ) {
            Text(text = "Add account")
        }
    }
}

@Composable
private fun AccountRow(
    buttonTrigger: () -> Unit,
    user: UserInfo,
    currentUser: Boolean,
    disableState: State<Boolean>
) {
    Row(
        modifier = if (currentUser) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val textColor = MaterialTheme.colorScheme.run {
            if (currentUser) onPrimary else primary
        }
        Text(
            text = user.name,
            modifier = Modifier
                .weight(1f)
                .padding(start = 2.dp, end = 2.dp),
            textAlign = TextAlign.Center,
            color = textColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = user.state.toString(), modifier = Modifier.weight(5f), color = textColor)
        Button(
            onClick = buttonTrigger,
            enabled = !disableState.value
        ) {
            Text(text = if (!currentUser) "Switch" else "SignOut")
        }
    }
}

@Preview(device = "spec:parent=Nexus 10")
@Composable
fun PreviewContent() = RanobeReaderTheme {
    Content(
        modifier = Modifier.fillMaxSize(),
        currentId = 1,
        users = listOf(
            UserInfo(id = 0, "Анна", state = UserState.Admin),
            UserInfo(id = 1, "Кортес", state = UserState.User),
            UserInfo(id = 2, "Данил", state = UserState.User),
            UserInfo(id = 3, "Элеонора", state = UserState.User),
            UserInfo(id = 4, "Марк 2", state = UserState.User)
        ),
        showDialog = {},
        currentIdTrigger = { _ -> },
        disable = MutableStateFlow(true)
    )
}
