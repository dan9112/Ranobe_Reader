@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.lord_markus.ranobe_reader.auth.presentation

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lord_markus.ranobe_reader.auth.domain.models.AuthCheckResult
import com.lord_markus.ranobe_reader.auth.domain.repository.AuthRepository
import com.lord_markus.ranobe_reader.auth.domain.use_cases.GetSignedInUsersUseCase
import com.lord_markus.ranobe_reader.auth.presentation.models.AuthScreenState
import com.lord_markus.ranobe_reader.auth.presentation.models.AuthUseCaseState
import com.lord_markus.ranobe_reader.auth.presentation.models.ExtendedAuthUseCaseState
import com.lord_markus.ranobe_reader.auth_core.domain.models.*
import com.lord_markus.ranobe_reader.auth_core.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth_core.domain.use_cases.SignUpUseCase
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.core.models.UserState
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme
import com.vdurmont.emoji.EmojiParser

private val inputRegex = Regex(pattern = "[\\s\n]+")

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier,
    onBackPressed: @Composable (() -> Unit) -> Unit,
    onSuccess: (List<UserInfo>, Long) -> Unit,
    primary: Boolean
) = ConstraintLayout(modifier = modifier) {
    val (indicator, content) = createRefs()
    val progressBarVisible = rememberSaveable { mutableStateOf(true) }

    Box(
        modifier = Modifier.constrainAs(content) {
            linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
            height = Dimension.fillToConstraints
            width = Dimension.fillToConstraints
        }
    ) {
        val authState by viewModel.authState.collectAsStateWithLifecycle()

        val switchIndicator = { state: Boolean ->
            progressBarVisible.value = state
        }

        when (val currentState = authState) {
            AuthUseCaseState.InProcess -> {
                progressBarVisible.value = true
            }

            is AuthUseCaseState.ResultReceived -> {
                progressBarVisible.value = false
                when (val result = currentState.result) {
                    is AuthCheckResult.Error -> TODO(reason = "Показать ошибку и закрыть приложение")

                    AuthCheckResult.Success.NoSuchUsers -> {
                        val authScreenState by viewModel.authScreenState.collectAsStateWithLifecycle()

                        when (authScreenState) {
                            AuthScreenState.SignIn -> {
                                SignInScreen(
                                    viewModel = viewModel,
                                    onSuccess = {
                                        onSuccess(listOf(it), it.id)
                                    },
                                    switchIndicator = switchIndicator,
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
                                    switchIndicator = switchIndicator
                                )
                            }
                        }
                    }

                    is AuthCheckResult.Success.SignedIn -> {
                        if (primary) result.run {
                            Log.e("MyLog", "Caught signed in users: $signedIn")
                            onSuccess(signedIn, currentUserId)
                        }
                        else SignInScreen(
                            viewModel = viewModel,
                            onSuccess = {
                                result.run { onSuccess(listOf(it), currentUserId) }
                            },
                            switchIndicator = switchIndicator,
                            primary = primary
                        )
                    }
                }
            }
        }
    }

    CircularProgressIndicator(
        modifier = Modifier
            .constrainAs(indicator) {
                linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
            }
            .alpha(if (progressBarVisible.value) 1f else 0f)
    )
}

@Composable
private fun SignUpScreen(
    viewModel: AuthViewModel,
    onSuccess: (List<UserInfo>, Long) -> Unit,
    switchIndicator: (Boolean) -> Unit
) {
    val signUpState by viewModel.signUpState.collectAsStateWithLifecycle()

    var login by rememberSaveable { mutableStateOf(value = "") }
    var password by rememberSaveable { mutableStateOf(value = "") }
    var password2 by rememberSaveable { mutableStateOf(value = "") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var errorColor by rememberSaveable { mutableStateOf(value = false) }

    when (val currentState = signUpState) {
        AuthUseCaseState.InProcess -> {
            switchIndicator(true)
            Log.d("MyLog", "Process continues...")
        }

        ExtendedAuthUseCaseState.Default -> {
            switchIndicator(false)
            Log.d("MyLog", "Process has not been started yet")
        }

        is AuthUseCaseState.ResultReceived -> {
            switchIndicator(false)
            if (currentState.trigger) {
                viewModel.caughtTrigger()
                Log.d("MyLog", "Process finished")
                when (val result = currentState.result) {
                    is SignUpResultAuth.Error -> {
                        errorColor = true
                        Toast.makeText(
                            LocalContext.current,
                            when (val error = result.error) {
                                SignUpError.IncorrectInput -> stringResource(id = R.string.incorrect_input)
                                SignUpError.LoginAlreadyInUse -> stringResource(id = R.string.login_is_already_in_use)
                                SignUpError.PasswordRequirements -> stringResource(id = R.string.invalid_password)
                                is AuthCoreUseCaseError.StorageError -> error.message// todo: добавить корректную обработку
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is SignUpResultAuth.Success -> {
                        Log.e("MyLog", "Signed up user: ${result.userInfo}")
                        result.run {
                            onSuccess(listOf(userInfo), userInfo.id)
                        }
                    }
                }
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
        val action: () -> Unit = {
            viewModel.trySignUp(login, password, password2)
            keyboardController?.hide()
        }
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
    viewModel: AuthViewModel,
    onSuccess: (UserInfo) -> Unit,
    switchIndicator: (Boolean) -> Unit,
    primary: Boolean
) {
    val signInState by viewModel.signInState.collectAsStateWithLifecycle()

    var login by rememberSaveable { mutableStateOf(value = "") }
    var password by rememberSaveable { mutableStateOf(value = "") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var errorColor by rememberSaveable { mutableStateOf(value = false) }

    when (val currentState = signInState) {
        ExtendedAuthUseCaseState.Default -> {
            switchIndicator(false)
            Log.d("MyLog", "Process has not been started yet")
        }

        AuthUseCaseState.InProcess -> {
            switchIndicator(true)
            Log.d("MyLog", "Process continues...")
        }

        is AuthUseCaseState.ResultReceived -> {
            switchIndicator(false)
            if (currentState.trigger) {
                viewModel.caughtTrigger()
                Log.d("MyLog", "Process finished")
                when (val result = currentState.result) {
                    is SignInResultAuth.Error -> {
                        errorColor = true
                        Toast.makeText(
                            LocalContext.current,
                            when (val error = result.error) {
                                SignInError.IncorrectInput -> stringResource(id = R.string.incorrect_input)
                                SignInError.NoSuchUser -> stringResource(id = R.string.no_such_user)
                                is AuthCoreUseCaseError.StorageError -> error.message
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is SignInResultAuth.Success -> {
                        Log.i("MyLog", "Texts: $login\t$password")
                        Log.e("MyLog", "Signed in user: ${result.userInfo}")
                        onSuccess(result.userInfo)
                        if (!primary) {
                            login = ""
                            password = ""
                        }
                    }
                }
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
            enabled = signInState != AuthUseCaseState.InProcess,
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
            viewModel.trySignIn(login = login, password = password, update = primary)
            keyboardController?.hide()
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
            enabled = signInState != AuthUseCaseState.InProcess,
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
            enabled = signInState != AuthUseCaseState.InProcess
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
        onSuccess = { _, _ -> },
        switchIndicator = { }
    )
}

private val authRepositoryStub by lazy {
    object : AuthRepository {
        private val userInfoStub = UserInfo(id = 0, state = UserState.User)
        override suspend fun getSignedInUsers() = AuthCheckResult.Success.NoSuchUsers
        override suspend fun signIn(login: String, password: String, update: Boolean) =
            SignInResultAuth.Success(userInfo = userInfoStub)

        override suspend fun signUp(login: String, password: String, state: UserState, withSignIn: Boolean) =
            SignUpResultAuth.Success(userInfo = userInfoStub)
    }
}

private val viewModelStub by lazy {
    AuthViewModel(
        savedStateHandler = SavedStateHandle(),
        getSignedInUsersUseCase = GetSignedInUsersUseCase(authRepository = authRepositoryStub),
        signInUseCase = SignInUseCase(authRepository = authRepositoryStub),
        signUpUseCase = SignUpUseCase(authRepository = authRepositoryStub)
    )
}
