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

                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "auth") {
                        composable(route = "auth") {
                            Auth.Screen(
                                modifier = Modifier.fillMaxSize(),
                                onBackPressed = { BackHandler { it() } },
                                onSuccess = {
                                    val json = Uri.encode(Json.encodeToString(it))
                                    navController.navigate("main/$json") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                        composable(
                            route = "main/{users}",
                            arguments = listOf(
                                navArgument(name = "users") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->
                            val users = backStackEntry
                                .arguments
                                ?.getString("users")
                                ?.let { Json.decodeFromString<List<UserInfo>>(it) } ?: emptyList()
                            Log.e("MyLog", "Input users: $users")
                            Main.Screen(
                                modifier = Modifier.fillMaxSize(),
                                users = users,
                                updateSignedIn = {
                                    if (it.isNotEmpty()) {
                                        val json = Uri.encode(Json.encodeToString(it))
                                        navController.navigate(route = "main/$json") {
                                            launchSingleTop = true
                                        }
                                    } else {
                                        navController.navigate(route = "auth") {
                                            launchSingleTop = true
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
