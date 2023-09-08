package com.lord_markus.ranobe_reader.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lord_markus.ranobe_reader.auth.Auth
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme
import com.lord_markus.ranobe_reader.main.Main
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RootActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RanobeReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: RootViewModel = hiltViewModel()
                    val navController = rememberNavController()

                    LaunchedEffect(Unit) {
                        viewModel.navigate.collect {
                            it?.let { hasSignedIn ->
                                with(receiver = navController) {
                                    when (hasSignedIn) {
                                        true -> navigate("main") {
                                            popUpTo("auth") {
                                                inclusive = true
                                            }
                                        }

                                        false -> navigate("auth") {
                                            popUpTo("main") {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(Unit) {
                        navController.currentBackStack.collect {
                            Log.d("MyLog", "Current backStack:\n${it.joinToString(separator = "\n")}")
                        }
                    }
                    NavHost(navController = navController, startDestination = "auth") {
                        composable(route = "auth") {
                            Log.v("MyLog", "Auth Destination")

                            Auth.Screen(
                                modifier = Modifier.fillMaxSize(),
                                onBackPressed = {
                                    BackHandler { it() }
                                },
                                onSuccess = { signedIn, currentId ->
                                    Log.i("MyLog", "Auth success!")
                                    with(receiver = viewModel) {
                                        updateUsersAndCurrent(signedIn, currentId)
                                    }
                                }
                            )
                        }
                        composable(route = "main") { backStackEntry ->
                            Log.v("MyLog", "Main Destination")

                            with(receiver = viewModel) {
                                Main.Screen(
                                    modifier = Modifier.fillMaxSize(),
                                    usersWithCurrentState = signedInWithCurrent.collectAsStateWithLifecycle(),
                                    addUser = { user, newCurrent ->
                                        signedInWithCurrent.value.run {
                                            updateUsersAndCurrent(
                                                (first + user).sortedBy { it.name },
                                                if (newCurrent) user.id else second
                                            )
                                        }
                                    },
                                    removeUser = { users ->
                                        signedInWithCurrent.value.run {
                                            updateUsersAndCurrent(users, users.firstOrNull()?.id)
                                        }
                                    },
                                    updateCurrent = { newCurrentId ->
                                        updateUsersAndCurrent(signedInWithCurrent.value.first, newCurrentId)
                                        Log.i("MyLog", "Update with: $newCurrentId")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
