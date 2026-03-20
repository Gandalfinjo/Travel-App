package com.example.travelapp.worker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/**
 * Utility object for managing notification channels.
 *
 * Creates and configures Android notification channels for trip reminders.
 */
object NotificationHelper {
    const val CHANNEL_ID = "trip_reminder_channel"
    const val CHANNEL_NAME = "Trip Reminders"

    /**
     * Creates a high-priority notification channel for trip reminders.
     * Should be called once during application initialization.
     *
     * @param context Application context
     */
    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for upcoming trips"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}