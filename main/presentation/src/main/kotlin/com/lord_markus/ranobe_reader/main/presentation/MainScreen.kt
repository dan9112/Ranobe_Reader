package com.lord_markus.ranobe_reader.main.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onBackPressed: @Composable (() -> Unit) -> Unit,
    goOut: () -> Unit
) = ConstraintLayout {
    val (indicator, content) = createRefs()
    val progressBarVisible = rememberSaveable { mutableStateOf(false) }

    Content(
        modifier = Modifier
            .constrainAs(content) {
                linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            },
        goOut = goOut
    )

    if (progressBarVisible.value) CircularProgressIndicator(
        modifier = Modifier
            .constrainAs(indicator) {
                linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
            }
    )
}

@Composable
fun Content(
    modifier: Modifier = Modifier,
    goOut: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome, user!")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { goOut() }) {
            Text(text = "Exit")
        }
    }
}
