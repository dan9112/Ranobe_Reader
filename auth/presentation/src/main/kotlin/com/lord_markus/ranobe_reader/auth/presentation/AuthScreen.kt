package com.lord_markus.ranobe_reader.auth.presentation

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lord_markus.ranobe_reader.auth.domain.models.*
import com.lord_markus.ranobe_reader.auth.domain.repository.Repository
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignInUseCase
import com.lord_markus.ranobe_reader.auth.domain.use_cases.SignUpUseCase
import com.lord_markus.ranobe_reader.auth.presentation.models.UseCaseState

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        when (val currentState = authState) {
            UseCaseState.InProcess -> Log.d("MyLog", "Process continues...")// show indicator
            UseCaseState.Default -> Log.d("MyLog", "Process has not been started yet")// hide indicator
            is UseCaseState.ResulReceived -> {
                Log.d("MyLog", "Process finished")// hide indicator
                when (val result = currentState.result) {
                    is SignInResult.Error -> Log.d("MyLog", "It caught error:\n${result.error}")
                    is SignInResult.Success -> Log.d("MyLog", "UserInfo:\n${result.userInfo}")
                }
            }
        }
    }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                viewModel.tryLogIn(username, password)
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
