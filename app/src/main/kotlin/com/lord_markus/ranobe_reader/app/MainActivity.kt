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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
                                    val json = Uri.encode(Json.encodeToString(signedIn.sortedBy { it.id }))
                                    navController.navigate("main/$json/$currentId") {
                                        popUpTo("auth") {
                                            inclusive = true
                                        }
                                    }
                                }
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
                            Log.v("MyLog", "Main Destination")
                            val args = backStackEntry
                                .arguments
                            val users = args
                                ?.getString("users")
                                ?.let { Json.decodeFromString<List<UserInfo>>(it) }
                                ?: throw IllegalArgumentException("Empty signed in list!")
                            if (!args.containsKey("current")) throw IllegalArgumentException("Empty current id!")
                            val currentId: Long = args.getLong("current")

                            Main.Screen(
                                modifier = Modifier.fillMaxSize(),
                                users = users,
                                currentId = currentId,
                                updateSignedIn = { newUsers, newCurrentId ->
                                    Log.i("MyLog", "Update with: $newCurrentId\n${newUsers.joinToString()}")
                                    val route = if (newUsers.isNotEmpty()) {
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
