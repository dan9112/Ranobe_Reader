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
    nightMode: Boolean?,
    updateNightMode: (Boolean?) -> Unit,
    dynamicMode: Boolean,
    updateDynamicMode: (Boolean) -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
    usersWithCurrentState: State<Pair<List<UserInfo>, Long?>>,
    addUser: (UserInfo, Boolean) -> Unit,
    removeUser: (List<UserInfo>) -> Unit,
    updateCurrent: (Long) -> Unit
) = with(receiver = viewModel) {
    Log.i("ComposeLog", "MainScreen")
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()


    val switchUserAction = { id: Long ->
        coroutineScope.launch {
            setCurrentFlow.collect {
                when (it) {
                    MainUseCaseState.InProcess -> {
                        switchProgressBar(true)
                    }

                    is MainUseCaseState.ResultReceived -> {
                        switchProgressBar(false)
                        when (it.result) {
                            is SetCurrentResultMain.Error -> Toast.makeText(
                                context,
                                "Attempt to switch account failed!",
                                Toast.LENGTH_SHORT
                            ).show()

                            SetCurrentResultMain.Success -> {
                                navController.resetNavGraph()
                                updateSelectedNavDrawerItem()
                                updateCurrent(id)
                            }
                        }
                    }
                }
            }
        }
        setCurrent(id)
    }

    val removeUserAction = {
        coroutineScope.launch {
            signOutFlow.collect {
                when (val currentState = it) {
                    MainUseCaseState.InProcess -> {
                        switchProgressBar(true)
                    }

                    is MainUseCaseState.ResultReceived -> when (val result = currentState.result) {
                        is SignOutResultMain.Error -> {
                            switchProgressBar(false)
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
                            if (list.isNotEmpty()) {
                                navController.resetNavGraph()
                                updateSelectedNavDrawerItem()
                            }
                            switchProgressBar(false)
                            removeUser(list)
                        }
                    }
                }
            }
        }
        signOut()
    }
    val authCoreScreenData = AuthCoreScreenData(
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
    )

    Screen(
        modifier = modifier,
        nightMode = nightMode,
        updateNightMode = updateNightMode,
        dynamicMode = dynamicMode,
        updateDynamicMode = updateDynamicMode,
        selectedNavDrawerItemState = selectedNavDrawerItem.collectAsStateWithLifecycle(),
        updateNavDrawerItem = ::updateSelectedNavDrawerItem,
        navController = navController,
        coroutineScope = coroutineScope,
        usersWithCurrentState = usersWithCurrentState,
        currentIdTrigger = switchUserAction,
        switchDialog = ::switchDialog,
        authCoreScreenData = authCoreScreenData,
        indicatorVisibleState = progressBarVisible.collectAsStateWithLifecycle(),
        dialogShowState = dialogInUse.collectAsStateWithLifecycle(),
        addUser = addUser,
        resetAuthCoreViewModel = ::resetAuthCoreViewModel,
        removeUser = removeUserAction
    )
}

private fun NavHostController.resetNavGraph() {
    graph.forEach {
        clearBackStack(it.id)
    }
    navigate("home") {
        popUpTo(graph.startDestinationId) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

@Composable
private fun Indicator(showState: State<Boolean>, modifier: Modifier) {
    Log.i("ComposeLog", "Indicator - ${showState.value}")
    if (showState.value) CircularProgressIndicator(modifier = modifier)
}

@Composable
private fun AuthDialog(
    users: List<UserInfo>,
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
                users = users,
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
    nightMode: Boolean?,
    updateNightMode: (Boolean?) -> Unit,
    dynamicMode: Boolean,
    updateDynamicMode: (Boolean) -> Unit,
    selectedNavDrawerItemState: State<Int?>,
    updateNavDrawerItem: (Int?) -> Unit,
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
    selectedNavDrawerItemState.value?.let { selectedItem ->
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
                            updateNavDrawerItem(null)
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
                                updateNavDrawerItem(index)
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
                    selected = selectedNavDrawerItemState.value == -1,
                    onClick = {
                        updateNavDrawerItem(-1)
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
                        updateNavDrawerItem(null)
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
                                updateNavDrawerItem(null)
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSecondary,
                        titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
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
                val (indicator, content, title) = createRefs()

                AuthDialog(
                    users = usersWithCurrentState.value.first,
                    showState = dialogShowState,
                    authCoreScreenData = authCoreScreenData,
                    resetAuthCoreViewModel = resetAuthCoreViewModel,
                    onSuccess = { user ->
                        switchDialog(false)
                        navController.resetNavGraph()
                        updateNavDrawerItem(null)
                        coroutineScope.launch {
                            navigationDrawerState.close()
                        }
                        addUser(user, true)
                    },
                    onDismiss = {
                        Log.i("MyLog", "Dialog dismissed")
                        switchDialog(false)
                    }
                )
                val text = when (selectedNavDrawerItemState.value) {
                    (-1) -> stringResource(id = R.string.settings)
                    0 -> stringResource(id = R.string.my_shelf)
                    1 -> stringResource(id = R.string.recommendations)
                    2 -> stringResource(id = R.string.history)
                    else -> ""
                }
                Text(
                    text = text,
                    modifier = Modifier
                        .constrainAs(ref = title) {
                            top.linkTo(parent.top)
                            linkTo(start = parent.start, end = parent.end)
                            height = if (text.isNotEmpty()) Dimension.wrapContent else Dimension.value(0.dp)
                            width = Dimension.fillToConstraints
                        }
                        .background(color = MaterialTheme.colorScheme.primaryContainer)
                        .padding(
                            start = if (selectedNavDrawerItemState.value != null) 20.dp else 0.dp,
                            top = if (selectedNavDrawerItemState.value != null) 8.dp else 0.dp,
                            bottom = if (selectedNavDrawerItemState.value != null) 8.dp else 0.dp
                        ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Start,
                    fontSize = 18.sp
                )

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.constrainAs(content) {
                        linkTo(start = parent.start, top = title.bottom, end = parent.end, bottom = parent.bottom)
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
                        Log.v("MyLog", "My shelf Destination")
                        MyShelfScreen()
                    }
                    composable(route = "recommendation") {
                        Log.v("MyLog", "Recommendations Destination")
                        RecommendationsScreen()
                    }
                    composable(route = "story") {
                        Log.v("MyLog", "History Destination")
                        HistoryScreen()
                    }
                    composable(route = "settings") {
                        Log.v("MyLog", "Settings Destination")
                        SettingsScreen(
                            nightMode = nightMode,
                            updateNightMode = updateNightMode,
                            dynamicMode = dynamicMode,
                            updateDynamicMode = updateDynamicMode
                        )
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
        resetSignInState = {},
        resetSignUpTrigger = {},
        resetSignUpState = {},
        switchAuthCoreProgressBar = {},
        indicatorShowFlow = MutableStateFlow(false)
    )
    val selectedItemState = remember { mutableStateOf<Int?>(null) }

    Screen(
        modifier = Modifier.fillMaxSize(),
        nightMode = null,
        updateNightMode = {},
        dynamicMode = true,
        updateDynamicMode = {},
        selectedNavDrawerItemState = selectedItemState,
        updateNavDrawerItem = { selectedItemState.value = it },
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
