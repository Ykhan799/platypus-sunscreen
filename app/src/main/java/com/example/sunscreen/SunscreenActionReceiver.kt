package com.example.sunscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.edit

class SunscreenActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CONFIRMED = "com.example.sunscreen.ACTION_CONFIRMED"
        const val ACTION_SNOOZE    = "com.example.sunscreen.ACTION_SNOOZE"
        private const val PREF     = "sunscreen_prefs"
        private const val KEY_NEXT = "next_allowed_time"
        private const val TWO_HOURS_MS   = 20 * 1000L    // 2 h
        private const val FIFTEEN_MIN_MS =  10 * 1000L        // 15 min
    }

    override fun onReceive(context: Context, intent: Intent) {
        val delta = when (intent.action) {
            ACTION_CONFIRMED -> TWO_HOURS_MS
            ACTION_SNOOZE    -> FIFTEEN_MIN_MS
            else             -> 0L
        }
        if (delta > 0) {
            val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            prefs.edit {
                putLong(KEY_NEXT, System.currentTimeMillis() + delta)
            }
        }

        // Dismiss the notification immediately
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager
        nm.cancel(1)
    }
}
