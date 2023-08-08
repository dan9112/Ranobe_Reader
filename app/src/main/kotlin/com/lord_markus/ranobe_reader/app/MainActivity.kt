package com.lord_markus.ranobe_reader.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.lord_markus.ranobe_reader.auth.Auth
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme

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
                    // todo:
                    //  - добавить главное окно
                    //  - добавить навигацию
                    //  - добавить переход между окнами
                    //  - добавить Hilt в модуль главного окна
                    Auth.Screen(
                        onBackPressed = { onBackAction: () -> Unit ->
                            BackHandler { onBackAction() }
                        },
                        onSuccess = { userInfo ->
                            Toast.makeText(LocalContext.current, userInfo.toString(), Toast.LENGTH_SHORT).show()
                        }
                    )
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
