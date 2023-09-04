package com.lord_markus.ranobe_reader.app

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lord_markus.ranobe_reader.auth.Auth
import com.lord_markus.ranobe_reader.core.models.UserInfo
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme
import com.lord_markus.ranobe_reader.main.Main
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RanobeReaderTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // val viewModel: MainViewModel = viewModel()
                    val navController = rememberNavController()

                    /*LaunchedEffect(Unit) {
                        navController.currentBackStack.collect {
                            Log.println(ASSERT, "MyLog", "Current backStack:\n${it.joinToString(separator = "\n")}")
                        }
                    }*/
                    NavHost(navController = navController, startDestination = "auth") {
                        composable(route = "auth") {
                            Auth.Screen(
                                modifier = Modifier.fillMaxSize(),
                                onBackPressed = {
                                    BackHandler { it() }
                                },
                                onSuccess = { signedIn, currentId ->
                                    val json = Uri.encode(Json.encodeToString(signedIn.sortedBy { it.id }))
                                    navController.navigate("main/$json/$currentId") {
                                        popUpTo("auth") {
                                            inclusive = true
                                        }
                                    }
                                },
                                primary = true
                            )
                        }
                        composable(
                            route = "main/{users}/{current}",
                            arguments = listOf(
                                navArgument(name = "users") {
                                    type = NavType.StringType
                                },
                                navArgument(name = "current") {
                                    type = NavType.LongType
                                }
                            )
                        ) { backStackEntry ->
                            val users = backStackEntry
                                .arguments
                                ?.getString("users")
                                ?.let { Json.decodeFromString<List<UserInfo>>(it) }
                                ?: throw IllegalArgumentException("Empty signed in list!")
                            val currentId = backStackEntry
                                .arguments
                                ?.getLong("current") ?: throw IllegalArgumentException("Empty current id!")
                            Log.i("MyLog", "Input users: $users")

                            val openDialog = remember { mutableStateOf(false) }
                            if (openDialog.value) {
                                Dialog(onDismissRequest = { openDialog.value = false }) {
                                    Auth.Screen(
                                        modifier = Modifier.fillMaxSize(),
                                        onBackPressed = {
                                            openDialog.value = false
                                        },
                                        onSuccess = { list, newCurrentId ->
                                            val json =
                                                Uri.encode(Json.encodeToString((users + list).sortedBy { it.id }))
                                            navController.navigate("main/$json/$newCurrentId") {
                                                popUpTo("main/{users}/{current}") {
                                                    inclusive = true
                                                }
                                            }
                                        },
                                        primary = false
                                    )
                                }
                            }

                            Main.Screen(
                                modifier = Modifier.fillMaxSize(),
                                addUsers = {
                                    openDialog.value = true
                                },
                                onBackPressed = {
                                    BackHandler {
                                        it()
                                        finish()
                                    }
                                },
                                users = users,
                                currentId = currentId,
                                updateSignedIn = { newUsers, newCurrentId ->
                                    val route: String
                                    route = if (newUsers.isNotEmpty()) {
                                        val json = Uri.encode(Json.encodeToString(newUsers))
                                        "main/$json/$newCurrentId"
                                    } else {
                                        "auth"
                                    }
                                    navController.navigate(route) {
                                        popUpTo("main/{users}/{current}") {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) = Text(
    text = "Hello $name!",
    modifier = modifier
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() = RanobeReaderTheme {
    Greeting("Android")
}
