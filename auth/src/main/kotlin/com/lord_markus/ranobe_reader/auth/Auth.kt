package com.lord_markus.ranobe_reader.auth

import android.util.Log
import androidx.compose.runtime.Composable
import com.lord_markus.ranobe_reader.auth.presentation.AuthScreen
import com.lord_markus.ranobe_reader.auth.presentation.AuthViewModel
import org.koin.androidx.compose.koinViewModel

data object Auth {
    @Composable
    fun Screen() {
        Log.e("MyLog", "Screen invoke")
        AuthScreen(getViewModel = { koinViewModel<AuthViewModel>() })
    }
}
