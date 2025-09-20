package com.example.travelapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.travelapp.database.models.enums.TripStatus
import com.example.travelapp.database.repositories.TripRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class EndTripWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val tripRepository: TripRepository
): CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val tripId = inputData.getInt("tripId", -1)

        if (tripId == -1) return Result.failure()

        return try {
            tripRepository.updateTripStatus(tripId, TripStatus.FINISHED)
            Result.success()
        }
        catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}