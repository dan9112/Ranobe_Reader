@file:OptIn(ExperimentalMaterial3Api::class)

package ru.example.alarm_manager

import android.app.AlarmManager
import android.app.AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
import android.app.PendingIntent
import android.app.PendingIntent.getForegroundService
import android.app.PendingIntent.getService
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Build.VERSION_CODES.S
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import ru.example.alarm_manager.ui.theme.RanobeReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alarmManager = getSystemService(AlarmManager::class.java)
        val dataStoreManager = DataStoreManager(this)

        setContent {
            RanobeReaderTheme {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when {
                        SDK_INT < S -> {
                            ContentCompat(alarmManager = alarmManager)
                        }

                        SDK_INT < TIRAMISU -> {
                            val permissionFlag by dataStoreManager.getFlag().collectAsState(initial = true)
                            LaunchedEffect(Unit) {
                                if (alarmManager.canScheduleExactAlarms()) launch {
                                    dataStoreManager.saveFlag(false)
                                }
                            }

                            ContentS__S_V2(
                                alarmManager = alarmManager,
                                permissionFlag = permissionFlag,
                                updatePermissionFlag = dataStoreManager::saveFlag
                            )
                        }

                        else -> {
                            ContentTiramisu(alarmManager = alarmManager)
                        }
                    }
                }
            }
        }
    }

    @Composable
    @RequiresApi(TIRAMISU)
    private fun ContentTiramisu(alarmManager: AlarmManager) =
        /*
            Используется разрешение USE_EXACT_ALARM, которое предоставляется
            по факту наличия и пользователь не может его отозвать
            Важно! Использовать разрешение можно только для будильников и подобных приложений,
            критичных ко времени выполнения!
        */
        ContentCompat(alarmManager = alarmManager)

    @Composable
    @RequiresApi(S)
    private fun ContentS__S_V2(
        alarmManager: AlarmManager,
        permissionFlag: Boolean,
        updatePermissionFlag: suspend (Boolean) -> Unit
    ) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        fun setAlarm(alarmManager: AlarmManager, timeInMillis: Long) {
            val intent = Intent(context, AlarmService::class.java)
            val pendingIntent = getService(
                context,
                0,
                intent
            )
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        }

        var showDialog by rememberSaveable { mutableStateOf<Boolean?>(null) }// null - диалог ещё ни разу не показывался пользователю за сессию
        var exactSchedulerNotGrantedState by remember { mutableStateOf(permissionFlag && !alarmManager.canScheduleExactAlarms()) }
        // показать диалог, если за сессию ещё ни разу не показывал
        if (exactSchedulerNotGrantedState && showDialog == null) showDialog = true


        SystemBroadcastReceiver(systemAction = ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
            exactSchedulerNotGrantedState =
                permissionFlag && !alarmManager.canScheduleExactAlarms()
            // не менять флаг при null, так как планируется показать его при первом выключении разрешения за сессию
            if (!exactSchedulerNotGrantedState && showDialog == true) showDialog = false
        }

        MainScreen(
            generateAlarm = {
                setAlarm(alarmManager, it)
            },
            cancelAlarms = {
                alarmManager.cancel(
                    getService(
                        context,
                        1,
                        Intent(context, AlarmService::class.java)
                    )
                )
            },
            exactAllowed = !exactSchedulerNotGrantedState
        )

        if (showDialog == true) Dialog(onDismissRequest = { showDialog = false }) {
            val shape = RoundedCornerShape(16.dp)

            Card(shape = shape) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var notAskAgainState by rememberSaveable { mutableStateOf(false) }
                    Text(text = stringResource(R.string.alert), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.exact_alarm_permission_definition),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = notAskAgainState,
                            onCheckedChange = { notAskAgainState = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.do_not_ask_again))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val buttonTrigger = {
                            showDialog = false

                            if (notAskAgainState) coroutineScope.launch(Dispatchers.IO) {
                                updatePermissionFlag(false)
                            }
                        }

                        Button(onClick = buttonTrigger) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                buttonTrigger()

                                startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                            }
                        ) {
                            Text(text = stringResource(R.string.grant))
                        }
                    }
                }
            }
        }
    }

    private fun getService(context: Context, requestCode: Int, intent: Intent) = if (SDK_INT < O) getService(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )
    else getForegroundService(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    @Composable
    private fun ContentCompat(alarmManager: AlarmManager) {
        val context = LocalContext.current

        fun setAlarmCompat(alarmManager: AlarmManager, timeInMillis: Long) {
            val intent = Intent(context, AlarmService::class.java)
            val pendingIntent = getService(
                context,
                0,
                intent
            )
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }


        MainScreen(
            generateAlarm = {
                setAlarmCompat(alarmManager, it)
            },
            cancelAlarms = {
                alarmManager.cancel(
                    getService(
                        context,
                        1,
                        Intent(context, AlarmService::class.java)
                    )
                )
            },
            exactAllowed = true
        )
    }
}

@Composable
private fun MainScreen(generateAlarm: (Long) -> Unit, cancelAlarms: () -> Unit, exactAllowed: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val currentTimeZone = TimeZone.currentSystemDefault()
        val state = Clock.System.now()
            .toLocalDateTime(currentTimeZone)
            .run {
                rememberTimePickerState(initialHour = hour, initialMinute = minute)
            }

        TimePicker(state = state)
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                generateAlarm(
                    Clock.System.now()
                        .let { systemInstant ->
                            systemInstant.toLocalDateTime(currentTimeZone)
                                .run {
                                    LocalDateTime(year, monthNumber, dayOfMonth, state.hour, state.minute)
                                        .toInstant(currentTimeZone)
                                }
                                .let { alarmInstant ->
                                    if (alarmInstant > systemInstant) {
                                        alarmInstant
                                    } else {
                                        alarmInstant.plus(value = 1, unit = DateTimeUnit.DAY, currentTimeZone)
                                    }
                                }
                                .toEpochMilliseconds()
                        }
                )
            },
            border = if (!exactAllowed) {
                BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.secondary)
            } else {
                null
            }
        ) {
            Text(text = stringResource(R.string.create))
        }
        if (!exactAllowed) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.not_exact),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = cancelAlarms) {
            Text(text = stringResource(R.string.cancel))
        }
    }
}

@Preview(wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE, showBackground = true, locale = "ru")
@Composable
fun MainScreenPreview() = RanobeReaderTheme {
    MainScreen(
        generateAlarm = {},
        cancelAlarms = {},
        exactAllowed = true
    )
}

@Preview(apiLevel = S, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE, showBackground = true, locale = "ru")
@Composable
fun MainScreenSdk_S__S_V2_Preview() = RanobeReaderTheme {
    MainScreen(
        generateAlarm = {},
        cancelAlarms = {},
        exactAllowed = true
    )
}

@Preview(apiLevel = S, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE, showBackground = true, locale = "ru")
@Composable
fun MainScreenSdk_S__S_V2_NotExactAlarmsPreview() = RanobeReaderTheme {
    MainScreen(
        generateAlarm = {},
        cancelAlarms = {},
        exactAllowed = false
    )
}
