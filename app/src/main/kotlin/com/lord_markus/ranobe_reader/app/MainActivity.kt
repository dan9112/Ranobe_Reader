package com.lord_markus.ranobe_reader.app

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lord_markus.ranobe_reader.auth.Auth
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme
import com.lord_markus.ranobe_reader.main.Main
import dagger.hilt.android.AndroidEntryPoint

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
                    val viewModel: MainViewModel = hiltViewModel()
                    val state = viewModel.userInfo.collectAsStateWithLifecycle()
                    // todo:
                    //  - добавить главное окно
                    //  - добавить навигацию
                    //  - добавить переход между окнами
                    //  - добавить Hilt в модуль главного окна
                    when (val signedIn = state.value) {
                        null -> {
                            Auth.Screen(
                                onBackPressed = { onBackAction: () -> Unit ->
                                    BackHandler { onBackAction() }
                                },
                                onSuccess = {
                                    with(receiver = viewModel) {
                                        setUsersInfo(currentState = it)
                                    }
                                }
                            )
                        }

                        else -> {
                            Log.e("MyLog", "Current user state: $signedIn")
                            Main.Screen(
                                onBackPressed = {},
                                goOut = { viewModel.removeUserInfo() }// todo: переработать функцию в дальнейшем!
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RanobeReaderTheme {
        Greeting("Android")
    }
}
