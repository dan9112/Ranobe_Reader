package com.lord_markus.ranobe_reader.settings.presentation

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.lord_markus.ranobe_reader.design.ui.theme.RanobeReaderTheme

@Composable
fun SettingsScreen(nightMode: Boolean?, dynamicMode: Boolean) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val baseModifier = Modifier.padding(start = 8.dp, end = 8.dp)
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                modifier = baseModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.night_mode),
                    modifier = Modifier.weight(1f)
                )
                ConstraintLayout(modifier = Modifier.weight(1f)) {
                    val (
                        defBack,
                        def,
                        notNightBack,
                        notNight,
                        nightBack,
                        night
                    ) = createRefs()
                    val barrier = createStartBarrier(def, notNight, night)
                    fun updateValue(value: Boolean?) {
                        viewModel.updateNightMode(value)
                    }

                    @Composable
                    fun Item(
                        value: Boolean?,
                        text: String,
                        modifierMain: Modifier,
                        modifierBack: Modifier
                    ) {
                        val interactionSource = remember { MutableInteractionSource() }
                        val onClick = { updateValue(value) }

                        Row(
                            modifier = modifierMain.wrapContentWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = nightMode == value,
                                onClick = onClick,
                                interactionSource = interactionSource
                            )
                            Text(text = text)
                        }
                        Spacer(
                            modifier = modifierBack
                                .clickable(
                                    onClick = onClick,
                                    indication = null,
                                    interactionSource = interactionSource
                                )
                        )
                    }

                    fun createAroundConstraintBlock(
                        main: ConstrainedLayoutReference,
                        before: Boolean?
                    ): ConstrainScope.() -> Unit = {
                        height = Dimension.fillToConstraints
                        width = Dimension.fillToConstraints
                        linkTo(
                            start = if (before == false) main.end else parent.start,
                            top = main.top,
                            end = if (before == true) main.start else parent.end,
                            bottom = main.bottom
                        )
                    }

                    fun createMainConstraintBlock(
                        top: ConstraintLayoutBaseScope.HorizontalAnchor? = null,
                        bottom: ConstraintLayoutBaseScope.HorizontalAnchor? = null
                    ): ConstrainScope.() -> Unit = {
                        height = Dimension.wrapContent
                        width = Dimension.wrapContent
                        linkTo(
                            start = barrier,
                            top = top ?: parent.top,
                            end = parent.end,
                            bottom = bottom ?: parent.bottom,
                            horizontalBias = 0f
                        )
                    }

                    Item(
                        value = null,
                        text = stringResource(R.string.like_in_the_system),
                        modifierMain = Modifier.constrainAs(def, createMainConstraintBlock(bottom = notNight.top)),
                        modifierBack = Modifier.constrainAs(
                            defBack,
                            createAroundConstraintBlock(main = def, before = null)
                        )
                    )
                    Item(
                        value = false,
                        text = stringResource(R.string.day),
                        modifierMain = Modifier.constrainAs(
                            notNight,
                            createMainConstraintBlock(top = def.bottom, bottom = night.top)
                        ),
                        modifierBack = Modifier.constrainAs(
                            notNightBack,
                            createAroundConstraintBlock(main = notNight, before = null)
                        )
                    )
                    Item(
                        value = true,
                        text = stringResource(R.string.night),
                        modifierMain = Modifier.constrainAs(night, createMainConstraintBlock(top = notNight.bottom)),
                        modifierBack = Modifier.constrainAs(
                            nightBack,
                            createAroundConstraintBlock(main = night, before = null)
                        )
                    )
                }
            }
        }
        if (SDK_INT >= S) {
            item {
                Divider(modifier = baseModifier.fillMaxWidth())
            }
            item {
                Row(
                    modifier = baseModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val onCheckedChange = viewModel::updateDynamicColor
                    Text(
                        text = stringResource(R.string.dynamic_color),
                        modifier = Modifier.weight(1f)
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                onCheckedChange(!dynamicMode)
                            },
                        horizontalArrangement = Arrangement.End
                    ) {
                        Switch(
                            checked = dynamicMode,
                            onCheckedChange = onCheckedChange,
                            interactionSource = interactionSource
                        )
                    }
                }
            }
        }
    }
}

@Preview(apiLevel = 33, wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview() {
    val nightMode by rememberSaveable { mutableStateOf<Boolean?>(false) }
    val dynamicMode by rememberSaveable { mutableStateOf(true) }
    RanobeReaderTheme(
        darkTheme = nightMode ?: isSystemInDarkTheme(),
        dynamicColor = dynamicMode
    ) {
        SettingsScreen(nightMode = nightMode, dynamicMode = dynamicMode)
    }
}