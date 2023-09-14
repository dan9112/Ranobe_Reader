@file:OptIn(ExperimentalMaterial3Api::class)

package com.lord_markus.ranobe_reader.main.presentation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lord_markus.ranobe_reader.auth_core.presentation.AuthCoreScreen
import com.lord_markus.ranobe_reader.auth_core.presentation.AuthCoreScreenData
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthScreenState
import com.lord_markus.ranobe_reader.auth_core.presentation.models.ExtendedAuthUseCaseState
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme
import com.lord_markus.ranobe_reader.history.HistoryScreen
import com.lord_markus.ranobe_reader.home.HomeScreen
import com.lord_markus.ranobe_reader.main.domain.models.MainUseCaseError
import com.lord_markus.ranobe_reader.main.domain.models.SetCurrentResultMain
import com.lord_markus.ranobe_reader.main.domain.models.SignOutResultMain
import com.lord_markus.ranobe_reader.main.presentation.models.MainUseCaseState
import com.lord_markus.ranobe_reader.main.presentation.models.NavigationDrawerItemData
import com.lord_markus.ranobe_reader.my_shelf.MyShelfScreen
import com.lord_markus.ranobe_reader.recommendations.RecommendationsScreen
import com.lord_markus.ranobe_reader.settings.SettingsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    usersWithCurrentState: State<Pair<List<UserInfo>, Long?>>,
    addUser: (UserInfo, Boolean) -> Unit,
    removeUser: (List<UserInfo>) -> Unit,
    updateCurrent: (Long) -> Unit
) {
    Log.i("ComposeLog", "MainScreen")
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    val switchUserAction = { id: Long ->
        with(receiver = viewModel) {
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

                                SetCurrentResultMain.Success -> updateCurrent(id)
                            }
                        }
                    }
                }
            }
            setCurrent(id)
        }
    }

    val removeUserAction = {
        coroutineScope.launch {
            viewModel.signOutFlow.collect {
                when (val currentState = it) {
                    MainUseCaseState.InProcess -> {
                        viewModel.switchProgressBar(true)
                    }

                    is MainUseCaseState.ResultReceived -> {
                        viewModel.switchProgressBar(false)
                        when (val result = currentState.result) {
                            is SignOutResultMain.Error -> {
                                if (result.trigger) {
                                    viewModel.caughtTrigger()
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
                                removeUser(list)
                                navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }
            }
        }
        viewModel.signOut()
    }
    val authCoreScreenData = viewModel.run {
        AuthCoreScreenData(
            authScreenFlow = authScreenFlow,
            switchAuthScreenState = ::switchAuthScreenState,
            signInState = signInState,
            signUpState = signUpState,
            trySignIn = ::trySignIn,
            trySignUp = ::trySignUp,
            resetSignInTrigger = ::resetSignInTrigger,
            resetSignUpTrigger = ::resetSignUpTrigger,
            switchAuthCoreProgressBar = ::switchAuthCoreProgressBar,
            indicatorShowFlow = authCoreProgressBarVisible
        )
    }

    Screen(
        modifier = modifier,
        navController = navController,
        coroutineScope = coroutineScope,
        usersWithCurrentState = usersWithCurrentState,
        currentIdTrigger = switchUserAction,
        switchDialog = viewModel::switchDialog,
        authCoreScreenData = authCoreScreenData,
        indicatorVisibleState = viewModel.progressBarVisible.collectAsStateWithLifecycle(),
        dialogShowState = viewModel.dialogInUse.collectAsStateWithLifecycle(),
        addUser = addUser,
        resetAuthCoreViewModel = viewModel::resetAuthCoreViewModel,
        removeUser = removeUserAction
    )
}

@Composable
private fun Indicator(showState: State<Boolean>, modifier: Modifier) {
    Log.i("ComposeLog", "Indicator - ${showState.value}")
    if (showState.value) CircularProgressIndicator(modifier = modifier)
}

@Composable
private fun AuthDialog(
    showState: State<Boolean>,
    authCoreScreenData: AuthCoreScreenData,
    resetAuthCoreViewModel: () -> Unit,
    onSuccess: (UserInfo) -> Unit,
    onDismiss: () -> Unit
) = if (showState.value) {
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
                authCoreScreenData = authCoreScreenData,
                onBackPressed = { onDismiss() },
                onSuccess = onSuccess,
                primary = false
            )
        }
    }
} else {
    resetAuthCoreViewModel()
}

@Composable
private fun Screen(
    modifier: Modifier,
    navController: NavHostController,
    coroutineScope: CoroutineScope,
    usersWithCurrentState: State<Pair<List<UserInfo>, Long?>>,
    currentIdTrigger: (Long) -> Unit,
    switchDialog: (Boolean) -> Unit,
    authCoreScreenData: AuthCoreScreenData,
    indicatorVisibleState: State<Boolean>,
    dialogShowState: State<Boolean>,
    addUser: (UserInfo, Boolean) -> Unit,
    removeUser: () -> Unit,
    resetAuthCoreViewModel: () -> Unit
) {
    val fontSize = 20.sp
    val navigationDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val navigationDrawerItemsData = listOf(
        NavigationDrawerItemData(
            icon = {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Navigation drawer settings item icon"
                )
            },
            titleRes = R.string.my_shelf,
            route = "mine",
            onClick = {
                /*todo: переключиться на свою полку*/
            }
        ),
        NavigationDrawerItemData(
            icon = {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Navigation drawer recommendations item icon"
                )
            },
            titleRes = R.string.recommendations,
            route = "recommendation",
            onClick = {
                /*todo: переключиться на рекомендации*/
            }
        ),
        NavigationDrawerItemData(
            icon = {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Navigation drawer history item icon"
                )
            },
            titleRes = R.string.history,
            route = "story",
            onClick = {
                /*todo: переключиться на историю*/
            }
        ),
    )
    val selectedDrawerItem = rememberSaveable { mutableStateOf<Int?>(null) }
    selectedDrawerItem.value?.let { selectedItem ->
        navigationDrawerItemsData.forEachIndexed { index, navigationDrawerItemData ->
            navigationDrawerItemData.selected = index == selectedItem
        }
    }
    ModalNavigationDrawer(
        drawerState = navigationDrawerState,
        drawerContent = {
            ModalDrawerSheet(drawerShape = RectangleShape) {
                Text(
                    "R@nobe Reader",
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.primary)
                        .clickable {
                            selectedDrawerItem.value = null
                            coroutineScope.launch {
                                navigationDrawerState.close()
                            }
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .fillMaxWidth()
                        .padding(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = fontSize,
                    fontFamily = FontFamily(
                        Font(
                            R.font.holitter_gothic,
                            FontWeight.Normal
                        )
                    ),
                    textAlign = TextAlign.Start
                )
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(navigationDrawerItemsData) { index, itemData ->
                        NavigationDrawerItem(
                            label = {
                                Text(stringResource(itemData.titleRes))
                            },
                            selected = itemData.selected,
                            onClick = {
                                selectedDrawerItem.value = index
                                navController.navigate(itemData.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                coroutineScope.launch {
                                    navigationDrawerState.close()
                                }
                                itemData.onClick()
                            },
                            icon = itemData.icon,
                            shape = RectangleShape
                        )
                    }
                }
                Divider(color = MaterialTheme.colorScheme.primary)
                NavigationDrawerItem(
                    label = {
                        Text(stringResource(R.string.settings))
                    },
                    selected = selectedDrawerItem.value == -1,
                    onClick = {
                        selectedDrawerItem.value = -1
                        coroutineScope.launch {
                            navigationDrawerState.close()
                        }
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Navigation drawer settings item icon"
                        )
                    },
                    shape = RectangleShape
                )
                Divider(color = MaterialTheme.colorScheme.primary)
                NavigationDrawerItem(
                    label = {
                        Text(stringResource(R.string.add_account))
                    },
                    selected = dialogShowState.value,
                    onClick = {
                        switchDialog(true)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Navigation drawer add account item icon"
                        )
                    },
                    shape = RectangleShape
                )
                NavigationDrawerItem(
                    label = {
                        Text(stringResource(R.string.log_out))
                    },
                    selected = false,
                    onClick = {
                        selectedDrawerItem.value = null
                        coroutineScope.launch {
                            navigationDrawerState.close()
                        }
                        removeUser()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Navigation drawer log out item icon"
                        )
                    },
                    shape = RectangleShape
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "R@nobe Reader",
                            modifier = Modifier.clickable {
                                selectedDrawerItem.value = null
                                navController.navigate("home") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            fontFamily = FontFamily(
                                Font(
                                    R.font.holitter_gothic,
                                    FontWeight.Normal
                                )
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    navigationDrawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Navigation icon"
                            )
                        }
                    },
                    actions = {
                        var expanded by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier.clickable(enabled = usersWithCurrentState.value.first.size > 1) {
                                expanded = !expanded
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Account image"
                            )

                            Text(
                                text = usersWithCurrentState.value.run {
                                    first.find { it.id == second }
                                }?.name.toString(),
                                fontSize = fontSize
                            )

                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                usersWithCurrentState.value.first.forEach { user ->
                                    if (user.id != usersWithCurrentState.value.second) AppBarAccount(
                                        buttonTrigger = {
                                            expanded = false
                                            currentIdTrigger(user.id)
                                        },
                                        fontSize = fontSize,
                                        user = user,
                                        currentUser = user.id == usersWithCurrentState.value.second,
                                        disableState = indicatorVisibleState.value
                                    )
                                }
                            }
                        }
                    }
                )
            }
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                val (indicator, content) = createRefs()

                AuthDialog(
                    showState = dialogShowState,
                    authCoreScreenData = authCoreScreenData,
                    resetAuthCoreViewModel = resetAuthCoreViewModel,
                    onSuccess = { user ->
                        addUser(user, true)
                        switchDialog(false)
                        coroutineScope.launch {
                            navigationDrawerState.close()
                        }
                    },
                    onDismiss = {
                        Log.i("MyLog", "Dialog dismissed")
                        switchDialog(false)
                    }
                )
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.constrainAs(content) {
                        linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
                        height = Dimension.fillToConstraints
                        width = Dimension.fillToConstraints
                    }
                ) {
                    composable(route = "home") {
                        Log.v("MyLog", "Home Destination")

                        HomeScreen(
                            name = usersWithCurrentState.value.run {
                                first.find { it.id == second }
                            }!!.name
                        )
                    }
                    composable(route = "mine") {
                        Log.v("MyLog", "Settings Destination")
                        MyShelfScreen()
                    }
                    composable(route = "recommendation") {
                        Log.v("MyLog", "Settings Destination")
                        RecommendationsScreen()
                    }
                    composable(route = "story") {
                        Log.v("MyLog", "Settings Destination")
                        HistoryScreen()
                    }
                    composable(route = "settings") {
                        Log.v("MyLog", "Settings Destination")
                        SettingsScreen()
                    }
                }

                Indicator(
                    modifier = Modifier
                        .constrainAs(indicator) {
                            linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
                        },
                    showState = indicatorVisibleState
                )
            }
        }
    }
}

@Composable
private fun AppBarAccount(
    buttonTrigger: (Boolean) -> Unit,
    fontSize: TextUnit,
    user: UserInfo,
    currentUser: Boolean,
    disableState: Boolean
) = Text(
    text = user.name,
    modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = !disableState) { buttonTrigger(!currentUser) }
        .padding(all = 4.dp),
    textAlign = TextAlign.Center,
    fontSize = fontSize
)

@Preview(device = "spec:parent=Nexus 10")
@Composable
fun PreviewContent() = RanobeReaderTheme {
    val usersWithCurrentState = remember {
        mutableStateOf<Pair<List<UserInfo>, Long?>>(
            listOf(
                UserInfo(id = 0, name = "Анна", state = UserState.Admin),
                UserInfo(id = 1, name = "Кортес", state = UserState.User),
                UserInfo(id = 2, name = "Данил", state = UserState.User),
                UserInfo(id = 3, name = "Элеонора", state = UserState.User),
                UserInfo(id = 4, name = "Марк 2", state = UserState.User)
            ).sortedBy { it.name } to 1
        )
    }
    val progressBarState = remember { mutableStateOf(false) }
    val dialogShowState = remember { mutableStateOf(false) }

    val authCoreScreenData = AuthCoreScreenData(
        authScreenFlow = MutableStateFlow(AuthScreenState.SignIn),
        switchAuthScreenState = {},
        signInState = MutableStateFlow(ExtendedAuthUseCaseState.Default),
        signUpState = MutableStateFlow(ExtendedAuthUseCaseState.Default),
        trySignIn = { _, _, _ -> },
        trySignUp = { _, _, _ -> },
        resetSignInTrigger = {},
        resetSignUpTrigger = {},
        switchAuthCoreProgressBar = {},
        indicatorShowFlow = MutableStateFlow(false)
    )

    Screen(
        modifier = Modifier.fillMaxSize(),
        navController = rememberNavController(),
        coroutineScope = CoroutineScope(Dispatchers.Main),
        usersWithCurrentState = usersWithCurrentState,
        currentIdTrigger = {},
        switchDialog = {},
        authCoreScreenData = authCoreScreenData,
        indicatorVisibleState = progressBarState,
        dialogShowState = dialogShowState,
        addUser = { _, _ -> },
        resetAuthCoreViewModel = {},
        removeUser = {}
    )
}
