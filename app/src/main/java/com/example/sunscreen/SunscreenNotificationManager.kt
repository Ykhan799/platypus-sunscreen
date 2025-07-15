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

class SunscreenNotificationManager(
    private val context: Context,
    private val sensorViewModel: SensorViewModel,
    private val scope: CoroutineScope
) {

    /* -------------------------------------------------------------------- */
    /*  constants                                                           */
    /* -------------------------------------------------------------------- */
    private companion object {
        const val CHANNEL_ID         = "uv_alert_channel"
        const val NOTIFICATION_ID    = 1
        const val PREF_FILE          = "sunscreen_prefs"
        const val PREF_KEY_NEXT_TIME = "next_allowed_time"
        const val TWO_HOURS_MS       = 10 * 1000L   // 2 hours
        const val FIFTEEN_MIN_MS     =  20 * 1000L       // 15 minutes
    }

    /* -------------------------------------------------------------------- */
    /*  state                                                               */
    /* -------------------------------------------------------------------- */
    private val prefs =
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    private var notificationJob: Job? = null

    /* -------------------------------------------------------------------- */
    /*  public API                                                          */
    /* -------------------------------------------------------------------- */
    fun start() {
        createNotificationChannel()

        notificationJob = scope.launch {
            while (isActive) {
                if (sensorViewModel.uvExposed.value == true) maybeNotify()
                delay(10_000)               // keep the loop inexpensive
            }
        }
    }

    fun stop() {
        notificationJob?.cancel()
    }

    /* -------------------------------------------------------------------- */
    /*  implementation                                                      */
    /* -------------------------------------------------------------------- */
    /** Called every tick; shows a notification if we’re past the silence window. */
    private fun maybeNotify() {
        val now = System.currentTimeMillis()
        val nextAllowed = prefs.getLong(PREF_KEY_NEXT_TIME, 0L)
        if (now >= nextAllowed) {
            sendNotification()
        }
    }

    /** Builds and posts the actionable notification. */
    private fun sendNotification() {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as AndroidNotificationManager

        /* ----- pend‑intents for the action buttons --------------------- */

        val yesIntent = Intent(context, SunscreenActionReceiver::class.java).apply {
            action = SunscreenActionReceiver.ACTION_CONFIRMED
        }
        val yesPending: PendingIntent = PendingIntent.getBroadcast(
            context, 101, yesIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, SunscreenActionReceiver::class.java).apply {
            action = SunscreenActionReceiver.ACTION_SNOOZE
        }
        val snoozePending: PendingIntent = PendingIntent.getBroadcast(
            context, 102, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        /* ----- main tap → open app ------------------------------------- */

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPending: PendingIntent = PendingIntent.getActivity(
            context, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        /* ----- build the notification ---------------------------------- */

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.warning)           // replace with your icon
            .setContentTitle("High UV exposure detected")
            .setContentText("Did you apply sunscreen?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPending)
            .setOngoing(true)
            .addAction(
                R.drawable.check,                       // ✅ replace icon
                "I applied",
                yesPending
            )
            .addAction(
                R.drawable.warning,                     // 🕒 replace icon
                "Remind in 15 min",
                snoozePending
            )
            .setTicker("UV Alert!")
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }

    /** One‑time channel registration (required on Android 8+). */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as AndroidNotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "UV Alerts",
            AndroidNotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications that remind you to apply sunscreen."
        }

        nm.createNotificationChannel(channel)
    }
}
