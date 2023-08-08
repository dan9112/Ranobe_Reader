package com.lord_markus.ranobe_reader.main.presentation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.lord_markus.ranobe_reader.core.models.UserInfo

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onBackPressed: @Composable (() -> Unit) -> Unit,
    onSuccess: @Composable (UserInfo) -> Unit
) {

}
