package com.example.travelapp.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.travelapp.R

class TripReminderWorker(
    context: Context,
    params: WorkerParameters
): Worker(context, params) {
    companion object {
        const val CHANNEL_ID = "trip_notifications"
        const val CHANNEL_NAME = "Trip Reminders"
        const val NOTIFICATION_ID = 100

        const val KEY_TRIP_ID = "trip_id"
        const val KEY_TRIP_NAME = "trip_name"
        const val KEY_DAYS_BEFORE = "days_before"
    }

    override fun doWork(): Result {
        val tripId = inputData.getInt(KEY_TRIP_ID, -1)

        if (tripId == -1) return Result.failure()

        val tripName = inputData.getString(KEY_TRIP_NAME) ?: "Trip"
        val daysBefore = inputData.getInt(KEY_DAYS_BEFORE, 0)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Reminders for upcoming trips"
        }
        notificationManager.createNotificationChannel(channel)

        val title = "$daysBefore-Day Reminder: $tripName"
        val content = "Your trip starts in $daysBefore days! Pack your bags."
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val uniqueNotificationId = NOTIFICATION_ID + tripId.hashCode() + daysBefore
        notificationManager.notify(uniqueNotificationId, notification)

        return Result.success()
    }
}