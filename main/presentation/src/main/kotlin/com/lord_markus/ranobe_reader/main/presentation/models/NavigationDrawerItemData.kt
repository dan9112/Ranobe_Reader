package com.lord_markus.ranobe_reader.main.presentation.models

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable

data class NavigationDrawerItemData(
    val icon: @Composable (() -> Unit)? = null,
    @StringRes val titleRes: Int,
    val onClick: () -> Unit,
    var selected: Boolean = false
)
