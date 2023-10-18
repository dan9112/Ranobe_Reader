@file:OptIn(ExperimentalMaterial3Api::class)

package ru.example.alarm_manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.getForegroundService
import android.app.PendingIntent.getService
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteConstraintException
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.datetime.*
import ru.example.alarm_manager.database.AlarmDatabase
import ru.example.alarm_manager.database.AlarmDb
import ru.example.alarm_manager.ui.theme.RanobeReaderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val alarmManager = getSystemService(AlarmManager::class.java)
        val dataStoreManager = DataStoreManager(this)
        val database = Room.databaseBuilder(
            applicationContext,
            AlarmDatabase::class.java,
            "alarmDatabase"
        )
            .allowMainThreadQueries()// todo: заменить в дальнейшем
            .build()
        val dao = database.alarmDao()

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
                    var lastId by rememberSaveable { mutableStateOf(dao.getLastId()) }
                    /*when {
                        SDK_INT < S -> {*/
                    ContentCompat(
                        alarmManager = alarmManager,
                        insertToDb = {
                            dao.insert(it).toInt().also { newValue ->
                                lastId = newValue
                            }
                        },
                        lastId = lastId
                    )
                    /*}

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
                                updatePermissionFlag = dataStoreManager::saveFlag,
                                insertToDb = dao::add,
                                deleteFromDb = dao::delete
                            )
                        }

                        else -> {
                            ContentTiramisu(
                                alarmManager = alarmManager,
                                insertToDb = dao::add,
                                deleteFromDb = dao::delete
                            )
                        }
                    }*/
                }
            }
        }
    }

    /*@Composable
    @RequiresApi(TIRAMISU)
    private fun ContentTiramisu(alarmManager: AlarmManager, insertToDb: (AlarmDb) -> Int, deleteFromDb: (Int) -> Unit) =
        *//*
            Используется разрешение USE_EXACT_ALARM, которое предоставляется
            по факту наличия и пользователь не может его отозвать
            Важно! Использовать разрешение можно только для будильников и подобных приложений,
            критичных ко времени выполнения!
        *//*
        ContentCompat(alarmManager = alarmManager, insertToDb = insertToDb, deleteFromDb = deleteFromDb)

    @Composable
    @RequiresApi(S)
    private fun ContentS__S_V2(
        alarmManager: AlarmManager,
        permissionFlag: Boolean,
        updatePermissionFlag: suspend (Boolean) -> Unit,
        insertToDb: (AlarmDb) -> Int,
        deleteFromDb: (Int) -> Unit
    ) {
        val context = applicationContext
        val coroutineScope = rememberCoroutineScope()
        var requestCode = 3

        fun setAlarm(timeInMillis: Long) {
            val intent = Intent(context, AlarmService::class.java)
            val pendingIntent = getService(
                context,
                requestCode++,
                intent
            )
            if (alarmManager.canScheduleExactAlarms()) {
//                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            } else {
//                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
                alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
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

        CreateScreen(
            generateAlarm = {
                dao.add()
                setAlarm(it)
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
    }*/

    private fun getService(context: Context, requestCode: Int, intent: Intent) = if (SDK_INT < O) getService(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
    )
    else getForegroundService(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
    )

    @Composable
    private fun ContentCompat(
        alarmManager: AlarmManager,
        insertToDb: (AlarmDb) -> Int,
        lastId: Int?
    ) {
        val context = applicationContext

        fun setAlarms(
            id: Int,
            time: LocalTime,
            weekTriggers: WeekTriggers
        ) {
            val (startDelay, today) = getFirstTime(time)

            val dayOfWeek = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .dayOfWeek
            val week = Week(startDay = dayOfWeek, weeksTriggers = weekTriggers)
            if (!today) week.moveStartNext()

            var currentId = id
            var currentStartDelay = startDelay
            week.forEach {
                if (it.trigger) {
                    val intent = Intent(context, AlarmService::class.java)
                        .putExtra("id", currentId)
                    val pendingIntent = getService(
                        context,
                        currentId,
                        intent
                    )
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, currentStartDelay, pendingIntent)
                }
                currentStartDelay += DatePeriod(days = 7).seconds * 1_000
                currentId++
            }
        }


        CreateScreen(
            generateAlarm = { localTime: LocalTime, weekTriggers: WeekTriggers ->
                val id = lastId?.let { it + 7 } ?: 0
                weekTriggers.run {
                    insertToDb(
                        AlarmDb(
                            id = id,
                            localTime = localTime,
                            monday = monday,
                            tuesday = tuesday,
                            wednesday = wednesday,
                            thursday = thursday,
                            friday = friday,
                            saturday = saturday,
                            sunday = sunday,
                            title = "$id"
                        )
                    )
                }
                setAlarms(id, localTime, weekTriggers)
                id
            },/*
            cancelAlarms = {
                deleteFromDb(it)
                alarmManager.cancel(
                    getService(
                        context,
                        1,
                        Intent(context, AlarmService::class.java)
                    )
                )
            },*/
            exactAllowed = true
        )
    }

    private fun getFirstTime(localTime: LocalTime): Pair<Long, Boolean> {
        val instant = Clock.System.now()
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val alarm = localDateTime.run {
            LocalDateTime(year, month, dayOfMonth, localTime.hour, localTime.minute)
        }
        return alarm.toInstant(TimeZone.currentSystemDefault()).run {
            if (alarm < localDateTime) plus(1, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
                .toEpochMilliseconds() to false
            else toEpochMilliseconds() to true
        }
    }
}

@Composable
private fun AlarmListScreen(
    list: List<AlarmDb>,
    updateAlarm: (Int) -> Unit,
    createAlarm: () -> Unit
) {
    LazyColumn {
        items(list) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        updateAlarm(item.id)
                    }
            ) {
                Text(item.localTime.run { "$hour:$minute" })
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = item.title)
                Spacer(modifier = Modifier.width(4.dp))
                val text = item.run {
                    StringBuilder().run {
                        when {
                            monday && tuesday && wednesday && thursday && friday -> {
                                if (saturday && sunday) append("Каждый день")
                                else if (!(saturday || sunday)) append("Будни")
                                else {
                                    append("Пн, вт, ср, чт, пт")
                                    if (saturday) append(", сб")
                                    if (sunday) append(", вс")
                                }
                            }

                            !monday && !tuesday && !wednesday && !thursday && !friday -> {
                                append(
                                    if (saturday && sunday) "Выходные"
                                    else if (saturday) "Сб"
                                    else if (sunday) "Вс"
                                    else "Выключен"
                                )
                            }

                            monday -> {
                                append("Пн")
                                if (tuesday) append(", вт")
                                if (wednesday) append(", ср")
                                if (thursday) append(", чт")
                                if (friday) append(", пт")
                                if (saturday) append(", сб")
                                if (sunday) append(", вс")
                            }

                            tuesday -> {
                                append("Вт")
                                if (wednesday) append(", ср")
                                if (thursday) append(", чт")
                                if (friday) append(", пт")
                                if (saturday) append(", сб")
                                if (sunday) append(", вс")
                            }

                            wednesday -> {
                                append("Ср")
                                if (thursday) append(", чт")
                                if (friday) append(", пт")
                                if (saturday) append(", сб")
                                if (sunday) append(", вс")
                            }

                            thursday -> {
                                append("Чт")
                                if (friday) append(", пт")
                                if (saturday) append(", сб")
                                if (sunday) append(", вс")
                            }

                            else -> {
                                append("Пт")
                                if (saturday) append(", сб")
                                if (sunday) append(", вс")

                            }
                        }
                        append('!')
                        toString()
                    }
                }
                Text(text)
            }
        }
        item {// todo: временное решение!
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = createAlarm
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Новый сигнал тревоги!",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EditScreen(
    hour: Int,
    minute: Int,
    updateAlarm: (LocalTime, WeekTriggers) -> Int,
    cancelAlarm: () -> Unit,
    exactAllowed: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val timeState = rememberTimePickerState(initialHour = hour, initialMinute = minute)

        val monday = rememberSaveable { mutableStateOf(false) }
        val tuesday = rememberSaveable { mutableStateOf(false) }
        val wednesday = rememberSaveable { mutableStateOf(false) }
        val thursday = rememberSaveable { mutableStateOf(false) }
        val friday = rememberSaveable { mutableStateOf(false) }
        val saturday = rememberSaveable { mutableStateOf(false) }
        val sunday = rememberSaveable { mutableStateOf(false) }

        ManageScreen(
            timeState = timeState,
            monday = monday,
            tuesday = tuesday,
            wednesday = wednesday,
            thursday = thursday,
            friday = friday,
            saturday = saturday,
            sunday = sunday
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                try {
                    updateAlarm(
                        LocalTime(hour = timeState.hour, minute = timeState.minute),
                        WeekTriggers(
                            monday.value,
                            tuesday.value,
                            wednesday.value,
                            thursday.value,
                            friday.value,
                            saturday.value,
                            sunday.value
                        )
                    )
                } catch (e: SQLiteConstraintException) {
                    e.printStackTrace()
                }
            },
            border = if (!exactAllowed) {
                BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.secondary)
            } else {
                null
            }
        ) {
            Text(text = "Обновить!")
        }
        if (!exactAllowed) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.not_exact),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = cancelAlarm) {
            Text(text = "Отменить!")
        }
    }
}

@Composable
private fun ManageScreen(
    timeState: TimePickerState,
    monday: MutableState<Boolean>,
    tuesday: MutableState<Boolean>,
    wednesday: MutableState<Boolean>,
    thursday: MutableState<Boolean>,
    friday: MutableState<Boolean>,
    saturday: MutableState<Boolean>,
    sunday: MutableState<Boolean>
) {
    val scrollState = rememberScrollState(0)

    TimePicker(state = timeState)
    Spacer(modifier = Modifier.height(8.dp))
    Row {
        DayElement(textRes = R.string.monday_short, state = monday)
        Spacer(modifier = Modifier.width(8.dp))
        DayElement(textRes = R.string.tuesday_short, state = tuesday)
        Spacer(modifier = Modifier.width(8.dp))
        DayElement(textRes = R.string.wednesday_short, state = wednesday)
        Spacer(modifier = Modifier.width(8.dp))
        DayElement(textRes = R.string.thursday_short, state = thursday)
        Spacer(modifier = Modifier.width(8.dp))
        DayElement(textRes = R.string.friday_short, state = friday)
        Spacer(modifier = Modifier.width(8.dp))
        DayElement(textRes = R.string.saturday_short, state = saturday)
        Spacer(modifier = Modifier.width(8.dp))
        DayElement(textRes = R.string.sunday_short, state = sunday)
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.horizontalScroll(scrollState)) {
        Button(
            onClick = {
                monday.value = true
                tuesday.value = true
                wednesday.value = true
                thursday.value = true
                friday.value = true
                saturday.value = false
                sunday.value = false
            }
        ) {
            Text(text = "Будни!")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                monday.value = false
                tuesday.value = false
                wednesday.value = false
                thursday.value = false
                friday.value = false
                saturday.value = true
                sunday.value = true
            }
        ) {
            Text(text = "Выходные!")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                monday.value = true
                tuesday.value = true
                wednesday.value = true
                thursday.value = true
                friday.value = true
                saturday.value = true
                sunday.value = true
            }
        ) {
            Text(text = "Каждый день!")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                monday.value = false
                tuesday.value = false
                wednesday.value = false
                thursday.value = false
                friday.value = false
                saturday.value = false
                sunday.value = false
            }
        ) {
            Text(text = "Сброс!")
        }
    }
}

@Composable
private fun CreateScreen(
    generateAlarm: (LocalTime, WeekTriggers) -> Int,
    exactAllowed: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val currentTimeZone = TimeZone.currentSystemDefault()
        val timeState = Clock.System.now()
            .toLocalDateTime(currentTimeZone)
            .run {
                rememberTimePickerState(initialHour = hour, initialMinute = minute)
            }
        var lastId by rememberSaveable { mutableStateOf<Int?>(null) }

        val monday = rememberSaveable { mutableStateOf(false) }
        val tuesday = rememberSaveable { mutableStateOf(false) }
        val wednesday = rememberSaveable { mutableStateOf(false) }
        val thursday = rememberSaveable { mutableStateOf(false) }
        val friday = rememberSaveable { mutableStateOf(false) }
        val saturday = rememberSaveable { mutableStateOf(false) }
        val sunday = rememberSaveable { mutableStateOf(false) }

        ManageScreen(
            timeState = timeState,
            monday = monday,
            tuesday = tuesday,
            wednesday = wednesday,
            thursday = thursday,
            friday = friday,
            saturday = saturday,
            sunday = sunday
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                try {
                    lastId = generateAlarm(
                        LocalTime(hour = timeState.hour, minute = timeState.minute),
                        WeekTriggers(
                            monday.value,
                            tuesday.value,
                            wednesday.value,
                            thursday.value,
                            friday.value,
                            saturday.value,
                            sunday.value
                        )
                    )
                } catch (e: SQLiteConstraintException) {
                    e.printStackTrace()
                }
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
    }
}

@Composable
private fun DayElement(modifier: Modifier = Modifier, @StringRes textRes: Int, state: MutableState<Boolean>) = Column(
    modifier = modifier.clickable { state.value = !state.value },
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(text = stringResource(id = textRes), fontSize = 18.sp)
    Spacer(modifier = Modifier.height(4.dp))
    Checkbox(
        modifier = Modifier.size(30.dp),
        checked = state.value,
        onCheckedChange = null
    )
}

@Preview(wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE, showBackground = true, locale = "ru")
@Composable
fun CreateScreenPreview() = RanobeReaderTheme {
    CreateScreen(
        generateAlarm = { _, _ -> 0 },
        exactAllowed = true
    )
}

@Preview(apiLevel = S, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE, showBackground = true, locale = "ru")
@Composable
fun CreateScreenSdk_S__S_V2_Preview() = RanobeReaderTheme {
    CreateScreen(
        generateAlarm = { _, _ -> 0 },
        exactAllowed = true
    )
}

@Preview(apiLevel = S, wallpaper = Wallpapers.RED_DOMINATED_EXAMPLE, showBackground = true, locale = "ru")
@Composable
fun CreateScreenSdk_S__S_V2_NotExactAlarmsPreview() = RanobeReaderTheme {
    CreateScreen(
        generateAlarm = { _, _ -> 0 },
        exactAllowed = false
    )
}
