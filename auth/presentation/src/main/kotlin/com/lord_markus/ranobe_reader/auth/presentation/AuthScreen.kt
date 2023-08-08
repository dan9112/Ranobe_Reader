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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lord_markus.ranobe_reader.auth.domain.models.*
import com.lord_markus.ranobe_reader.auth.domain.repository.Repository
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignUpUseCase
import com.lord_markus.ranobe_reader.auth.presentation.models.AuthScreenState
import com.lord_markus.ranobe_reader.auth.presentation.models.UseCaseState
import com.lord_markus.ranobe_reader.core.UserInfo
import com.lord_markus.ranobe_reader.core.UserState
import com.vdurmont.emoji.EmojiParser

private val inputRegex = Regex(pattern = "[\\s\n]+")

@Composable
fun AuthScreen(
    getViewModel: @Composable () -> AuthViewModel,
    onBackPressed: @Composable (() -> Unit) -> Unit,
    onSuccess: @Composable (UserInfo) -> Unit
) {
    val viewModel = getViewModel()
    val authState by viewModel.authScreenState.collectAsStateWithLifecycle()

    when (authState) {
        AuthScreenState.SignIn -> SignInScreen(viewModel = viewModel, onSuccess = onSuccess)
        AuthScreenState.SignUp -> SignUpScreen(
            viewModel = viewModel,
            onSuccess = onSuccess,
            onBackPressed = onBackPressed
        )
    }
}

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSuccess: @Composable (UserInfo) -> Unit,
    onBackPressed: @Composable (() -> Unit) -> Unit
) {
    onBackPressed {
        viewModel.switchAuthScreenState()
    }

    val signUpState by viewModel.signUpState.collectAsStateWithLifecycle()

    var login by rememberSaveable { mutableStateOf(value = "") }
    var password by rememberSaveable { mutableStateOf(value = "") }
    var password2 by rememberSaveable { mutableStateOf(value = "") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var errorColor by rememberSaveable { mutableStateOf(value = false) }

    when (val currentState = signUpState) {
        UseCaseState.InProcess -> {
            Log.d("MyLog", "Process continues...")// show indicator
        }

        UseCaseState.Default -> {
            Log.d("MyLog", "Process has not been started yet")// hide indicator
        }

        is UseCaseState.ResultReceived -> {
            if (currentState.trigger) {
                viewModel.caughtTrigger()
                Log.d("MyLog", "Process finished")// hide indicator
                when (val result = currentState.result) {
                    is SignUpResult.Error -> {
                        errorColor = true
                        Toast.makeText(
                            LocalContext.current,
                            when (val error = result.error) {
                                SignUpError.IncorrectInput -> stringResource(id = R.string.incorrect_input)
                                SignUpError.LoginAlreadyInUse -> stringResource(R.string.login_is_already_in_use)
                                SignUpError.PasswordRequirements -> stringResource(R.string.invalid_password)
                                is ResultError.StorageError -> error.message
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is SignUpResult.Success -> {
                        onSuccess(result.userInfo)
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
fun SignInScreen(viewModel: AuthViewModel, onSuccess: @Composable (UserInfo) -> Unit) {
    val signInState by viewModel.signInState.collectAsStateWithLifecycle()

    var login by rememberSaveable { mutableStateOf(value = "") }
    var password by rememberSaveable { mutableStateOf(value = "") }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var errorColor by rememberSaveable { mutableStateOf(value = false) }

    when (val currentState = signInState) {
        UseCaseState.InProcess -> {
            Log.d("MyLog", "Process continues...")// show indicator
        }

        UseCaseState.Default -> {
            Log.d("MyLog", "Process has not been started yet")// hide indicator
        }

        is UseCaseState.ResultReceived -> {
            if (currentState.trigger) {
                viewModel.caughtTrigger()
                Log.d("MyLog", "Process finished")// hide indicator
                when (val result = currentState.result) {
                    is SignInResult.Error -> {
                        errorColor = true
                        Toast.makeText(
                            LocalContext.current,
                            when (val error = result.error) {
                                SignInError.IncorrectInput -> stringResource(id = R.string.incorrect_input)
                                SignInError.NoSuchUser -> stringResource(id = R.string.no_such_user)
                                is ResultError.StorageError -> error.message
                            },
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is SignInResult.Success -> {
                        onSuccess(result.userInfo)
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
            enabled = signInState != UseCaseState.InProcess,
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
            viewModel.trySignIn(login, password)
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
            enabled = signInState != UseCaseState.InProcess,
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
            enabled = signInState != UseCaseState.InProcess
        ) {
            Text(text = stringResource(R.string.login_verb))
        }
        val annotatedString = buildAnnotatedString {
            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                append(stringResource(R.string.sign_up))
            }
        }
        Text(
            text = annotatedString,
            modifier = Modifier.clickable {
                viewModel.switchAuthScreenState()
            }
        )
    }
}

@Preview(device = "spec:parent=Nexus 10")
@Composable
fun PreviewSignInScreen() {
    SignInScreen(
        viewModel = viewModelStub,
        onSuccess = {}
    )
}

@Preview(device = "spec:parent=Nexus 10")
@Composable
fun PreviewSignUpScreen() {
    SignUpScreen(
        viewModel = viewModelStub,
        onSuccess = {},
        onBackPressed = { }
    )
}

private val viewModelStub
    get() = AuthViewModel(
        savedStateHandler = SavedStateHandle(),
        signInUseCase = SignInUseCase(repository = repositoryStub),
        signUpUseCase = SignUpUseCase(repository = repositoryStub)
    )

private val repositoryStub
    get() = object : Repository {
        private val userInfoStub = UserInfo(id = 0, state = UserState.User)
        override suspend fun getSignedInUsers() = AuthCheckResult.Success(signedIn = emptyList(), currentUserId = 0L)
        override suspend fun signIn(login: String, password: String) = SignInResult.Success(userInfo = userInfoStub)
        override suspend fun signOut() = SignOutResult.Success
        override suspend fun signUp(login: String, password: String, state: UserState) =
            SignUpResult.Success(userInfo = userInfoStub)

        override suspend fun removeAccount(userId: Long) = RemoveAccountResult.Success
        override suspend fun setCurrent(id: Long) = SetCurrentResult.Success
    }
