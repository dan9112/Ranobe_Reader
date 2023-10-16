package ru.example.alarm_manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.os.Build.VERSION_CODES.Q
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import ru.example.alarm_manager.AlarmService.Companion.NotificationsInfo.CHANNEL_IMPORTANCE
import ru.example.alarm_manager.AlarmService.Companion.NotificationsInfo.ChannelDescriptionRes
import ru.example.alarm_manager.AlarmService.Companion.NotificationsInfo.ChannelIdRes
import ru.example.alarm_manager.AlarmService.Companion.NotificationsInfo.ChannelNameRes
import ru.example.alarm_manager.AlarmService.Companion.NotificationsInfo.NOTIFICATION_ID
import ru.example.alarm_manager.AlarmService.Companion.NotificationsInfo.getBuilder

private const val TAG = "AlarmService"

class AlarmService : Service() {
    private lateinit var notificationManager: NotificationManager
    private lateinit var handler: Handler
    override fun onCreate() {
        notificationManager = getSystemService(NotificationManager::class.java)
        handler = Handler(Looper.getMainLooper())
        if (SDK_INT >= O) createChannel()
        val message = "AlarmService.onCreate()"
        postHandlerToast(message)
        Log.i(TAG, message)
    }

    override fun onBind(intent: Intent): IBinder? {
        val message = "AlarmService.onBind()"
        postHandlerToast(message)
        Log.i(TAG, message)
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        val message = "AlarmService.onDestroy()"
        postHandlerToast(message)
        Log.i(TAG, message)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notification = getBuilder(context = this).build()
        when {
            SDK_INT < Q -> startForeground(NOTIFICATION_ID, notification)
            SDK_INT < UPSIDE_DOWN_CAKE -> @Suppress("Deprecation") startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_NONE
            )

            else -> startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE)
        }

        val message = "AlarmService.onStartCommand()"
        postHandlerToast(message)
        Log.i(TAG, message)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf(startId)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent): Boolean {
        val message = "AlarmService.onUnbind()"
        postHandlerToast(message)
        Log.i(TAG, message)
        return super.onUnbind(intent)
    }

    @RequiresApi(O)
    private fun createChannel() {
        val channel = NotificationChannel(
            getString(ChannelIdRes),
            getString(ChannelNameRes),
            CHANNEL_IMPORTANCE
        ).apply {
            description = getString(ChannelDescriptionRes)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun postHandlerToast(message: String) {
        handler.post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private companion object {
        data object NotificationsInfo {
            val ChannelIdRes = R.string.alarm_notifications
            val ChannelNameRes = R.string.alarm
            val ChannelDescriptionRes = R.string.notification_channel_for_worker_messages
            const val CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_MIN

            private const val MESSAGE_PRIORITY = NotificationCompat.PRIORITY_MIN
            private val ChannelDefaultTitleRes = R.string.alarm_notification_title
            private val vibratePattern = LongArray(5) { 1_000 }
            const val NOTIFICATION_ID = 69

            fun getBuilder(context: Context) =
                NotificationCompat.Builder(context, context.getString(ChannelIdRes))
                    .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                    .setContentTitle(context.getString(ChannelDefaultTitleRes))
                    .setContentText(
                        context.getString(R.string.alarm_notification_message)
                    )
                    .setPriority(MESSAGE_PRIORITY)
                    .setVibrate(vibratePattern)
        }
    }
}
