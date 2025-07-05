package com.example.sunscreen

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sunscreen.sensing.SensorViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SunscreenNotificationManager(private val context: Context, private val sensorViewModel: SensorViewModel, private var scope: CoroutineScope) {
    companion object {
        private const val MIN_ALERT_INTERVAL_MS = 10 * 1000L
    }
    private var lastAlertTimeMillis = 0L
    private var notificationJob: Job? = null

    // Check every so often (rather than be prompted by changing sensor data) in case the
    // sensor data doesn't change, but the user needs another reminder after N time.
    fun start() {
        createNotificationChannel()
        notificationJob = scope.launch {
            while (isActive) {
                if (sensorViewModel.uvExposed.value == true){
                    maybeNotify()
                }
                delay(1_000)
            }
        }
    }

    // Only annoy user after some time has passed
    fun maybeNotify() {
        val now = System.currentTimeMillis()
        if (now - lastAlertTimeMillis > MIN_ALERT_INTERVAL_MS) {
            sendNotification()
            lastAlertTimeMillis = now
        }
    }

    private fun sendNotification() {
        val channelId = "uv_alert_channel"
        val notificationId = 1
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "UV Alerts",
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts you when UV exposure is high"
            }
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.warning) // use an appropriate icon
            .setContentTitle("High UV Exposure Detected")
            .setContentText("Consider applying sunscreen or seeking shade.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setTicker("UV Alert!")
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "UV Alert Notifications"
            val descriptionText = "Notifications for UV exposure reminders"
            val importance = AndroidNotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("uv_alert_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun stop() {
        notificationJob?.cancel()
    }
}