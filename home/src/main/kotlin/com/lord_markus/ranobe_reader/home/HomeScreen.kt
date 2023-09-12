package com.lord_markus.ranobe_reader.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Composable
fun HomeScreen(name: String) = ConstraintLayout(modifier = Modifier.fillMaxSize()) {
    val (text) = createRefs()


    Text(
        modifier = Modifier.constrainAs(text) {
            linkTo(start = parent.start, top = parent.top, end = parent.end, bottom = parent.bottom)
            height = Dimension.wrapContent
            width = Dimension.wrapContent
        },
        text = stringResource(R.string.hello, name)
    )
}
