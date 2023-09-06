@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.lord_markus.ranobe_reader.auth_core.presentation

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth.domain.repository.AuthRepository
import com.lord_markus.ranobe_reader.auth_core.domain.models.*
import com.lord_markus.ranobe_reader.auth_core.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth_core.domain.use_cases.SignUpUseCase
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthScreenState
import com.lord_markus.ranobe_reader.auth_core.presentation.models.AuthUseCaseState
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme
import com.vdurmont.emoji.EmojiParser
import kotlinx.coroutines.flow.StateFlow

private val inputRegex = Regex(pattern = "[\\s\n]+")

@Composable
fun AuthCoreScreen(
    viewModel: AuthCoreViewModel,
    modifier: Modifier,
    onBackPressed: @Composable (() -> Unit) -> Unit,
    onSuccess: (UserInfo) -> Unit,
    primary: Boolean
) = ConstraintLayout(modifier = modifier) {
    Log.e("MyLog", "Auth Core Screen")
    val (content, indicator) = createRefs()

    Content(
        modifier = Modifier
            .constrainAs(content) {
                linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            },
        viewModel = viewModel,
        onBackPressed = onBackPressed,
        onSuccess = onSuccess,
        primary = primary
    )
    Indicator(
        show = viewModel.authCoreProgressBarVisible,
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
    viewModel: AuthCoreViewModel,
    onBackPressed: @Composable (() -> Unit) -> Unit,
    onSuccess: (UserInfo) -> Unit,
    primary: Boolean
) = Box(modifier = modifier) {
    val authScreenState by viewModel.authScreenState.collectAsStateWithLifecycle()

    when (authScreenState) {
        AuthScreenState.SignIn -> {
            SignInScreen(
                viewModel = viewModel,
                users = emptyList(),
                onSuccess = onSuccess,
                switchIndicator = viewModel::switchAuthCoreProgressBar,
                primary = primary
            )
        }

        AuthScreenState.SignUp -> {
            onBackPressed {
                viewModel.switchAuthScreenState()
            }

            SignUpScreen(
                viewModel = viewModel,
                onSuccess = onSuccess,
                switchIndicator = viewModel::switchAuthCoreProgressBar
            )
        }
    }
}

@Composable
private fun SignUpScreen(
    viewModel: AuthCoreViewModel,
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
        viewModel.signUpState.collect {
            if (it != AuthUseCaseState.InProcess) {
                if (it is AuthUseCaseState.ResultReceived) {
                    if (it.result is SignUpResultAuth.Error) enabled.value = true

                    Log.d("MyLog", "Process finished")
                    when (val result = it.result) {
                        is SignUpResultAuth.Error -> {
                            switchIndicator(false)
                            if (result.trigger) {
                                viewModel.resetSignUpTrigger()
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
                    enabled.value = true
                }
                if (it !is AuthUseCaseState.ResultReceived || it.result !is SignUpResultAuth.Success)
                    enabled.value = true
            } else {
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
                    errorColor = false
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
                    errorColor = false
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
        val action: () -> Unit = { viewModel.trySignUp(login, password, password2) }
        OutlinedTextField(
            value = password2,
            onValueChange = {
                val newValue = EmojiParser.removeAllEmojis(it).replace(regex = inputRegex, replacement = "")
                if (newValue != password2) {
                    errorColor = false
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
        Button(onClick = action) {
            Text(text = stringResource(R.string.sign_up))
        }
    }
}

@Composable
private fun SignInScreen(
    viewModel: AuthCoreViewModel,
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
        viewModel.signInState.collect {
            if (it != AuthUseCaseState.InProcess) {
//                switchIndicator(false)
                if (it is AuthUseCaseState.ResultReceived) {
                    if (it.result is SignInResultAuth.Error) enabled.value = true

                    Log.d("MyLog", "Process finished")
                    when (val result = it.result) {
                        is SignInResultAuth.Error -> {
                            switchIndicator(false)
                            enabled.value = true
                            if (result.trigger) {
                                viewModel.resetSignInTrigger()
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
                            Log.e("MyLog", "Signed in user: ${result.userInfo}")

                            result.userInfo.let {
                                if (!users.contains(it)) {
                                    onSuccess(it)
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.user_has_already_added),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    errorColor = true
                                }
                            }
                        }
                    }
                } else {
                    enabled.value = true
                    switchIndicator(false)
                }
            } else {
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
                    errorColor = false
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
            viewModel.trySignIn(login = login, password = password)
        }
        OutlinedTextField(
            value = password,
            onValueChange = {
                val newValue = EmojiParser.removeAllEmojis(it).replace(regex = inputRegex, replacement = "")
                if (newValue != password) {
                    errorColor = false
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
            enabled = enabled.value
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
                    viewModel.switchAuthScreenState()
                }
            )
        }
    }
}

@Preview(device = "spec:parent=Nexus 10")
@Composable
fun PreviewPrimarySignInScreen() = RanobeReaderTheme {
    SignInScreen(
        viewModel = viewModelStub,
        users = emptyList(),
        onSuccess = { },
        switchIndicator = { },
        primary = true
    )
}

@Preview(device = "spec:parent=Nexus 10")
@Composable
fun PreviewDefaultSignInScreen() = RanobeReaderTheme {
    SignInScreen(
        viewModel = viewModelStub,
        users = emptyList(),
        onSuccess = { },
        switchIndicator = { },
        primary = false
    )
}

@Preview(device = "spec:parent=Nexus 10")
@Composable
fun PreviewSignUpScreen() = RanobeReaderTheme {
    SignUpScreen(
        viewModel = viewModelStub,
        onSuccess = { _ -> },
        switchIndicator = { }
    )
}

private val authRepositoryStub by lazy {
    object : AuthRepository {
        private val userInfoStub = UserInfo(id = 0, name = "Маркус", state = UserState.User)
        override suspend fun getSignedInUsers() = AuthCheckResult.Success.NoSuchUsers
        override suspend fun signIn(login: String, password: String, update: Boolean) =
            SignInResultAuth.Success(userInfo = userInfoStub)

        override suspend fun signUp(login: String, password: String, state: UserState, withSignIn: Boolean) =
            SignUpResultAuth.Success(userInfo = userInfoStub)
    }
}

private val viewModelStub by lazy {
    @SuppressLint("StaticFieldLeak")
    object : AuthCoreViewModel(
        savedStateHandler = SavedStateHandle(),
        signInUseCase = SignInUseCase(authRepository = authRepositoryStub),
        signUpUseCase = SignUpUseCase(authRepository = authRepositoryStub)
    ) {}
}
