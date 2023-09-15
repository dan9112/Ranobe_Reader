@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.lord_markus.ranobe_reader.auth_core.presentation

import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.atLeastWrapContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lord_markus.ranobe_reader.auth_core.domain.models.*
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthScreenState
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthUseCaseState
import com.lord_markus.ranobe_reader.auth_core.presentation.models.ExtendedAuthUseCaseState
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme
import com.vdurmont.emoji.EmojiParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private val inputRegex = Regex(pattern = "[\\s\n]+")

@Composable
fun AuthCoreScreen(
    authCoreScreenData: AuthCoreScreenData,
    modifier: Modifier,
    onBackPressed: @Composable (() -> Unit) -> Unit,
    onSuccess: (UserInfo) -> Unit,
    users: List<UserInfo> = emptyList(),
    primary: Boolean
) = ConstraintLayout(modifier = modifier) {
    Log.i("MyLog", "Auth Core Screen")
    val (content, indicator) = createRefs()

    authCoreScreenData.run {
        Content(
            modifier = Modifier
                .constrainAs(content) {
                    linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
                    height = Dimension.fillToConstraints.atLeastWrapContent
                    width = Dimension.fillToConstraints.atLeastWrapContent
                },
            users = users,
            authScreenState = authScreenFlow.collectAsStateWithLifecycle(),
            switchAuthScreenState = switchAuthScreenState,
            signInState = signInState,
            signUpState = signUpState,
            trySignIn = trySignIn,
            trySignUp = trySignUp,
            resetSignInTrigger = resetSignInTrigger,
            resetSignInState = resetSignInState,
            resetSignUpTrigger = resetSignUpTrigger,
            resetSignUpState = resetSignUpState,
            switchAuthCoreProgressBar = switchAuthCoreProgressBar,
            onBackPressed = onBackPressed,
            onSuccess = onSuccess,
            primary = primary
        )
        Indicator(
            showState = indicatorShowFlow.collectAsStateWithLifecycle(),
            modifier = Modifier
                .constrainAs(indicator) {
                    linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
                }
        )
    }
}

@Composable
private fun Indicator(showState: State<Boolean>, modifier: Modifier) {
    if (showState.value) CircularProgressIndicator(modifier = modifier)
}

@Composable
private fun Content(
    modifier: Modifier,
    users: List<UserInfo>,
    authScreenState: State<AuthScreenState>,
    switchAuthScreenState: () -> Unit,
    signInState: StateFlow<ExtendedAuthUseCaseState<SignInResultAuth>>,
    signUpState: StateFlow<ExtendedAuthUseCaseState<SignUpResultAuth>>,
    trySignIn: (login: String, password: String, update: Boolean) -> Unit,
    trySignUp: (login: String, password: String, password2: String) -> Unit,
    resetSignInState: () -> Unit,
    resetSignInTrigger: () -> Unit,
    resetSignUpState: () -> Unit,
    resetSignUpTrigger: () -> Unit,
    switchAuthCoreProgressBar: (Boolean) -> Unit,
    onBackPressed: @Composable (() -> Unit) -> Unit,
    onSuccess: (UserInfo) -> Unit,
    primary: Boolean
) = Box(
    modifier = modifier.verticalScroll(rememberScrollState()),
    contentAlignment = Alignment.Center
) {
    when (authScreenState.value) {
        AuthScreenState.SignIn -> {
            SignInScreen(
                signInState = signInState,
                resetSignInTrigger = resetSignInTrigger,
                resetSignInState = resetSignInState,
                trySignIn = trySignIn,
                switchAuthScreenState = switchAuthScreenState,
                users = users,
                onSuccess = onSuccess,
                switchIndicator = switchAuthCoreProgressBar,
                primary = primary
            )
        }

        AuthScreenState.SignUp -> {
            onBackPressed {
                switchAuthScreenState()
            }

            SignUpScreen(
                signUpState = signUpState,
                resetSignUpState = resetSignUpState,
                resetSignUpTrigger = resetSignUpTrigger,
                trySignUp = trySignUp,
                onSuccess = onSuccess,
                switchIndicator = switchAuthCoreProgressBar
            )
        }
    }
}

@Composable
private fun SignUpScreen(
    signUpState: StateFlow<ExtendedAuthUseCaseState<SignUpResultAuth>>,
    resetSignUpTrigger: () -> Unit,
    resetSignUpState: () -> Unit,
    trySignUp: (login: String, password: String, password2: String) -> Unit,
    onSuccess: (UserInfo) -> Unit,
    switchIndicator: (Boolean) -> Unit
) {
    var login by rememberSaveable { mutableStateOf(value = "") }
    var password by rememberSaveable { mutableStateOf(value = "") }
    var password2 by rememberSaveable { mutableStateOf(value = "") }
    val focusManager = LocalFocusManager.current
    var errorColor by rememberSaveable { mutableStateOf(value = false) }
    val enabled = rememberSaveable { mutableStateOf(true) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        signUpState.collect {
            if (it != AuthUseCaseState.InProcess) {
                if (it is AuthUseCaseState.ResultReceived) {
                    if (it.result is SignUpResultAuth.Error) enabled.value = true

                    Log.d("MyLog", "Process finished")
                    when (val result = it.result) {
                        is SignUpResultAuth.Error -> {
                            switchIndicator(false)
                            if (result.trigger) {
                                resetSignUpTrigger()
                                errorColor = true
                                Toast.makeText(
                                    context,
                                    when (val error = result.error) {
                                        SignUpError.IncorrectInput -> context.getString(R.string.incorrect_input)
                                        SignUpError.LoginAlreadyInUse -> context.getString(R.string.login_is_already_in_use)
                                        SignUpError.PasswordRequirements -> context.getString(R.string.invalid_password)
                                        is AuthCoreUseCaseError.StorageError -> error.message// todo: добавить корректную обработку
                                    },
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        is SignUpResultAuth.Success -> {
                            Log.e("MyLog", "Signed up user: ${result.userInfo}")

                            onSuccess(result.userInfo)
                        }
                    }
                } else {
                    errorColor = false
                    enabled.value = true
                }
                if (it !is AuthUseCaseState.ResultReceived || it.result !is SignUpResultAuth.Success)
                    enabled.value = true
            } else {
                errorColor = false
                switchIndicator(true)
                enabled.value = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = if (errorColor) Color.Red else MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = if (errorColor) Color.Red else MaterialTheme.colorScheme.primary,
            focusedLabelColor = if (errorColor) Color.Red else MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = if (errorColor) Color.Red else MaterialTheme.colorScheme.primary
        )
        Text(text = stringResource(R.string.registration))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = login,
            onValueChange = {
                val newValue = EmojiParser.removeAllEmojis(it).replace(regex = inputRegex, replacement = "")
                if (newValue != login) {
                    resetSignUpState()
                    login = newValue
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            label = { Text(text = stringResource(R.string.username)) },
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            ),
            trailingIcon = {
                if (login.isNotBlank()) Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "clear text",
                    modifier = Modifier.clickable { login = "" },
                    tint = if (errorColor) Color.Red else LocalContentColor.current
                )
            },
            colors = colors
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                val newValue = EmojiParser.removeAllEmojis(it).replace(regex = inputRegex, replacement = "")
                if (newValue != password) {
                    resetSignUpState()
                    password = newValue
                }
            },
            label = { Text(text = stringResource(R.string.password)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            ),
            trailingIcon = {
                if (password.isNotBlank()) Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "clear text",
                    modifier = Modifier.clickable { password = "" },
                    tint = if (errorColor) Color.Red else LocalContentColor.current
                )
            },
            colors = colors
        )
        Spacer(modifier = Modifier.height(8.dp))
        val action: () -> Unit = { trySignUp(login, password, password2) }
        OutlinedTextField(
            value = password2,
            onValueChange = {
                val newValue = EmojiParser.removeAllEmojis(it).replace(regex = inputRegex, replacement = "")
                if (newValue != password2) {
                    resetSignUpState()
                    password2 = newValue
                }
            },
            label = { Text(text = stringResource(R.string.password_repeat)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardActions = KeyboardActions(onDone = { action() }),
            trailingIcon = {
                if (password2.isNotBlank()) Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "clear text",
                    modifier = Modifier.clickable { password2 = "" },
                    tint = if (errorColor) Color.Red else LocalContentColor.current
                )
            },
            colors = colors
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            enabled = login.isNotEmpty() && password.isNotEmpty() && password2.isNotEmpty(),
            onClick = action
        ) {
            Text(text = stringResource(R.string.sign_up))
        }
    }
}

@Composable
private fun SignInScreen(
    signInState: StateFlow<ExtendedAuthUseCaseState<SignInResultAuth>>,
    resetSignInTrigger: () -> Unit,
    resetSignInState: () -> Unit,
    trySignIn: (login: String, password: String, update: Boolean) -> Unit,
    switchAuthScreenState: () -> Unit,
    users: List<UserInfo>,
    onSuccess: (UserInfo) -> Unit,
    switchIndicator: (Boolean) -> Unit,
    primary: Boolean
) {
    val enabled = rememberSaveable { mutableStateOf(true) }
    var login by rememberSaveable { mutableStateOf(value = "") }
    var password by rememberSaveable { mutableStateOf(value = "") }
    val focusManager = LocalFocusManager.current
    var errorColor by rememberSaveable { mutableStateOf(value = false) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        signInState.collect {
            if (it != AuthUseCaseState.InProcess) {
                if (it is AuthUseCaseState.ResultReceived) {
                    if (it.result is SignInResultAuth.Error) enabled.value = true

                    Log.d("MyLog", "Process finished")
                    when (val result = it.result) {
                        is SignInResultAuth.Error -> {
                            switchIndicator(false)
                            enabled.value = true
                            if (result.trigger) {
                                resetSignInTrigger()
                                errorColor = true
                                Toast.makeText(
                                    context,
                                    when (val error = result.error) {
                                        SignInError.IncorrectInput -> context.getString(R.string.incorrect_input)
                                        SignInError.NoSuchUser -> context.getString(R.string.no_such_user)
                                        is AuthCoreUseCaseError.StorageError -> error.message
                                    },
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        is SignInResultAuth.Success -> {
                            Log.i("MyLog", "Texts: $login\t$password")
                            Log.i("MyLog", "Signed in user: ${result.userInfo}")

                            result.userInfo.let {
                                if (!users.contains(it)) {
                                    onSuccess(it)
                                } else {
                                    enabled.value = true
                                    errorColor = true
                                    switchIndicator(false)
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.user_has_already_added),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                } else {
                    errorColor = false
                    enabled.value = true
                    switchIndicator(false)
                }
            } else {
                errorColor = false
                switchIndicator(true)
                enabled.value = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val textContainerColor = if (errorColor) Color.Red else MaterialTheme.colorScheme.primary
        val colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = textContainerColor,
            unfocusedBorderColor = textContainerColor,
            focusedLabelColor = textContainerColor,
            unfocusedLabelColor = textContainerColor
        )
        Text(text = stringResource(R.string.login))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = login,
            onValueChange = {
                val newValue = EmojiParser.removeAllEmojis(it).replace(regex = inputRegex, replacement = "")
                if (newValue != login) {
                    resetSignInState()
                    login = newValue
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            label = { Text(text = stringResource(R.string.username)) },
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Next) }
            ),
            enabled = enabled.value,
            trailingIcon = {
                if (login.isNotBlank()) Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "clear text",
                    modifier = Modifier.clickable { login = "" },
                    tint = if (errorColor) Color.Red else LocalContentColor.current
                )
            },
            colors = colors
        )
        Spacer(modifier = Modifier.height(8.dp))
        val action: () -> Unit = {
            trySignIn(login, password, true)
        }
        OutlinedTextField(
            value = password,
            onValueChange = {
                val newValue = EmojiParser.removeAllEmojis(it).replace(regex = inputRegex, replacement = "")
                if (newValue != password) {
                    resetSignInState()
                    password = newValue
                }
            },
            label = { Text(text = stringResource(R.string.password)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardActions = KeyboardActions(
                onDone = { action() }
            ),
            enabled = enabled.value,
            trailingIcon = {
                if (password.isNotBlank()) Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "clear text",
                    modifier = Modifier.clickable { password = "" },
                    tint = if (errorColor) Color.Red else LocalContentColor.current
                )
            },
            colors = colors
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = action,
            enabled = enabled.value && login.isNotEmpty() && password.isNotEmpty()
        ) {
            Text(text = stringResource(id = if (primary) R.string.login_verb else R.string.add_user))
        }
        if (primary) buildAnnotatedString {
            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                append(stringResource(R.string.sign_up))
            }
        }.let { annotatedString ->
            Text(
                text = annotatedString,
                modifier = Modifier.clickable {
                    switchAuthScreenState()
                }
            )
        }
    }
}

@Preview(
    device = "spec:parent=pixel_5,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Composable
fun PreviewPrimarySignInScreenNotNight() {
    RanobeReaderTheme {
        SignInScreen(
            signInState = MutableStateFlow(
                AuthUseCaseState.ResultReceived(
                    result = SignInResultAuth.Success(
                        userInfo = UserInfo(id = 0, name = "Маркус", state = UserState.User)
                    )
                )
            ),
            resetSignInTrigger = {},
            resetSignInState = {},
            trySignIn = { _, _, _ -> },
            switchAuthScreenState = {},
            users = emptyList(),
            onSuccess = { },
            switchIndicator = { },
            primary = true
        )
    }
}

@Preview(
    device = "spec:parent=pixel_5,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Composable
fun PreviewPrimarySignInScreenNight() = RanobeReaderTheme {
    SignInScreen(
        signInState = MutableStateFlow(
            AuthUseCaseState.ResultReceived(
                result = SignInResultAuth.Success(
                    userInfo = UserInfo(id = 0, name = "Маркус", state = UserState.User)
                )
            )
        ),
        resetSignInTrigger = {},
        resetSignInState = {},
        trySignIn = { _, _, _ -> },
        switchAuthScreenState = {},
        users = emptyList(),
        onSuccess = { },
        switchIndicator = { },
        primary = true
    )
}

@Preview(
    device = "spec:parent=pixel_5,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Composable
fun PreviewDefaultSignInScreenNotNight() = RanobeReaderTheme {
    SignInScreen(
        signInState = MutableStateFlow(
            AuthUseCaseState.ResultReceived(
                result = SignInResultAuth.Success(
                    userInfo = UserInfo(id = 0, name = "Маркус", state = UserState.User)
                )
            )
        ),
        resetSignInTrigger = {},
        resetSignInState = {},
        trySignIn = { _, _, _ -> },
        switchAuthScreenState = {},
        users = emptyList(),
        onSuccess = { },
        switchIndicator = { },
        primary = false
    )
}

@Preview(
    device = "spec:parent=pixel_5,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Composable
fun PreviewDefaultSignInScreenNight() = RanobeReaderTheme {
    SignInScreen(
        signInState = MutableStateFlow(
            AuthUseCaseState.ResultReceived(
                result = SignInResultAuth.Success(
                    userInfo = UserInfo(id = 0, name = "Маркус", state = UserState.User)
                )
            )
        ),
        resetSignInTrigger = {},
        resetSignInState = {},
        trySignIn = { _, _, _ -> },
        switchAuthScreenState = {},
        users = emptyList(),
        onSuccess = { },
        switchIndicator = { },
        primary = false
    )
}

@Preview(
    device = "spec:parent=pixel_5,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Composable
fun PreviewSignUpScreenNotNight() = RanobeReaderTheme {
    SignUpScreen(
        signUpState = MutableStateFlow(
            AuthUseCaseState.ResultReceived(
                SignUpResultAuth.Success(
                    UserInfo(id = 0, name = "Маркус", state = UserState.User)
                )
            )
        ),
        resetSignUpTrigger = {},
        resetSignUpState = {},
        trySignUp = { _, _, _ -> },
        onSuccess = { },
        switchIndicator = { }
    )
}

@Preview(
    device = "spec:parent=pixel_5,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Composable
fun PreviewSignUpScreenNight() = RanobeReaderTheme {
    SignUpScreen(
        signUpState = MutableStateFlow(
            AuthUseCaseState.ResultReceived(
                SignUpResultAuth.Success(
                    UserInfo(id = 0, name = "Маркус", state = UserState.User)
                )
            )
        ),
        resetSignUpTrigger = {},
        resetSignUpState = {},
        trySignUp = { _, _, _ -> },
        onSuccess = { },
        switchIndicator = { }
    )
}
