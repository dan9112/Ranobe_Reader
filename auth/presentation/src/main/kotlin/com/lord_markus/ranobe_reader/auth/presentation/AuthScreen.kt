@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)

package com.lord_markus.ranobe_reader.auth.presentation

import android.util.Log
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lord_markus.ranobe_reader.auth.domain.models.*
import com.lord_markus.ranobe_reader.auth.domain.repository.Repository
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignUpUseCase
import com.lord_markus.ranobe_reader.auth.presentation.models.UseCaseState
import com.vdurmont.emoji.EmojiParser

private val inputRegex = Regex(pattern = "[\\s\n]+")

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    var login by rememberSaveable { mutableStateOf(value = "") }
    var password by rememberSaveable { mutableStateOf(value = "") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var errorColor by rememberSaveable { mutableStateOf(value = false) }

    LaunchedEffect(authState) {
        when (val currentState = authState) {
            UseCaseState.InProcess -> {
                Log.d("MyLog", "Process continues...")// show indicator
            }

            UseCaseState.Default -> {
                Log.d("MyLog", "Process has not been started yet")// hide indicator
            }

            is UseCaseState.ResulReceived -> {
                Log.d("MyLog", "Process finished")// hide indicator
                when (val result = currentState.result) {
                    is SignInResult.Error -> {
                        errorColor = true
                        Log.d("MyLog", "It caught error:\n${result.error}")
                    }

                    is SignInResult.Success -> {
                        Log.d("MyLog", "UserInfo:\n${result.userInfo}")
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
        Text(text = "Login")
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
            label = { Text("Username") },
            keyboardActions = KeyboardActions(
                onNext = { focusRequester.requestFocus() }
            ),
            enabled = authState != UseCaseState.InProcess,
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
            label = { Text("Password") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardActions = KeyboardActions(
                onDone = {
                    viewModel.tryLogIn(login, password)
                    keyboardController?.hide()
                }
            ),
            enabled = authState != UseCaseState.InProcess,
            modifier = Modifier.focusRequester(focusRequester),
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
            onClick = {
                viewModel.tryLogIn(login, password)
                keyboardController?.hide()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState != UseCaseState.InProcess
        ) {
            Text(text = "Login")
        }
    }
}

@Preview
@Composable
fun PreviewLoginScreen() {
    AuthScreen(
        AuthViewModel(
            signInUseCase = SignInUseCase(repository = repositoryStub),
            signUpUseCase = SignUpUseCase(repository = repositoryStub)
        )
    )
}

private val repositoryStub
    get() = object : Repository {
        override suspend fun getSignedInUsers() = AuthCheckResult.Success(signedIn = emptyList(), currentUserId = 0L)
        override suspend fun signIn(login: String, password: String) = SignInResult.Success(userInfo = UserState.User)
        override suspend fun signOut() = SignOutResult.Success
        override suspend fun signUp(login: String, password: String, state: UserState) = SignUpResult.Success
        override suspend fun removeAccount(userId: Long) = RemoveAccountResult.Success
        override suspend fun setCurrent(id: Long) = SetCurrentResult.Success
    }
