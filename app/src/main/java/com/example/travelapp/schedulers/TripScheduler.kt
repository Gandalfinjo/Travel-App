package com.example.travelapp.schedulers

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.travelapp.database.models.Trip
import com.example.travelapp.worker.EndTripWorker
import com.example.travelapp.worker.StartTripWorker
import com.example.travelapp.worker.TripReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages background scheduling for trip-related notifications and status updates.
 *
 * Uses WorkManager to schedule:
 * - Reminder notifications (3 days and 1 day before trip start)
 * - Automatic trip status transitions (PLANNED -> ONGOING -> FINISHED)
 */
@Singleton
class TripScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    /**
     * Schedules reminder notifications for a trip.
     *
     * Creates two notifications:
     * - 3 days before trip start
     * - 1 day before trip start
     *
     * If the notification time has already passed, triggers it immediately.
     *
     * @param trip The trip for which to schedule notifications
     */
    fun scheduleTripNotifications(trip: Trip) {
        val workManager = WorkManager.getInstance(context)

        val now = LocalDateTime.now(ZoneId.systemDefault())

        val startDateTime = trip.startDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()

        // 3 days before
        val threeDaysBefore = startDateTime.minusDays(3)
        if (threeDaysBefore.isBefore(now)) {
            triggerNotification(workManager, trip, 3)
        }
        else if (threeDaysBefore.isAfter(now)) {
            val delayThreeDays = Duration.between(now, threeDaysBefore).toMillis()
            scheduleNotification(workManager, trip, 3, delayThreeDays)
        }

        // 1 day before
        val oneDayBefore = startDateTime.minusDays(1)
        if (oneDayBefore.isBefore(now)) {
            triggerNotification(workManager, trip, 1)
        }
        else if (oneDayBefore.isAfter(now)) {
            val delayOneDay = Duration.between(now, oneDayBefore).toMillis()
            scheduleNotification(workManager, trip, 1, delayOneDay)
        }
    }

    private fun scheduleNotification(workManager: WorkManager, trip: Trip, daysBefore: Int, delayMillis: Long) {
        val data = Data.Builder()
            .putInt(TripReminderWorker.KEY_TRIP_ID, trip.id)
            .putString(TripReminderWorker.KEY_TRIP_NAME, trip.name)
            .putInt(TripReminderWorker.KEY_DAYS_BEFORE, daysBefore)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<TripReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("trip_${trip.id}_${daysBefore}days")
            .build()

        workManager.enqueueUniqueWork("trip_${trip.id}_${daysBefore}days", ExistingWorkPolicy.REPLACE, workRequest)
    }

    private fun triggerNotification(workManager: WorkManager, trip: Trip, daysBefore: Int) {
        val data = Data.Builder()
            .putInt(TripReminderWorker.KEY_TRIP_ID, trip.id)
            .putString(TripReminderWorker.KEY_TRIP_NAME, trip.name)
            .putInt(TripReminderWorker.KEY_DAYS_BEFORE, daysBefore)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<TripReminderWorker>()
            .setInitialDelay(0, TimeUnit.MILLISECONDS) // Trigger immediately
            .setInputData(data)
            .addTag("trip_${trip.id}_${daysBefore}days")
            .build()

        workManager.enqueueUniqueWork("trip_${trip.id}_${daysBefore}days", ExistingWorkPolicy.REPLACE, workRequest)
    }

    /**
     * Schedules automatic status updates for a trip.
     *
     * Creates workers that will:
     * - Change status to ONGOING on trip start date
     * - Change status to FINISHED on trip start date
     *
     * @param trip The trip for which to schedule status updates
     */
    fun scheduleTripStatusUpdates(trip: Trip) {
        val workManager = WorkManager.getInstance(context)

        // Start worker
        val startDelay = Duration.between(LocalDateTime.now(), trip.startDate.atStartOfDay()).toMillis()
        if (startDelay > 0) {
            val startWork = OneTimeWorkRequestBuilder<StartTripWorker>()
                .setInitialDelay(startDelay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("tripId" to trip.id))
                .build()
            workManager.enqueueUniqueWork("trip_${trip.id}_startTrip", ExistingWorkPolicy.REPLACE, startWork)
        }

        // End worker
        val endDelay = Duration.between(LocalDateTime.now(), trip.endDate.atStartOfDay()).toMillis()
        if (endDelay > 0) {
            val endWork = OneTimeWorkRequestBuilder<EndTripWorker>()
                .setInitialDelay(endDelay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("tripId" to trip.id))
                .build()
            workManager.enqueueUniqueWork("trip_${trip.id}_endTrip", ExistingWorkPolicy.REPLACE,endWork)
        }
    }
}