package com.example.travelapp.worker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationHelper {
    const val CHANNEL_ID = "trip_reminder_channel"
    const val CHANNEL_NAME = "Trip Reminders"

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