package com.example.travelapp.ui.stateholders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.travelapp.database.models.Trip
import com.example.travelapp.database.models.enums.TransportType
import com.example.travelapp.database.models.enums.TripStatus
import com.example.travelapp.database.repositories.TripRepository
import com.example.travelapp.worker.EndTripWorker
import com.example.travelapp.worker.StartTripWorker
import com.example.travelapp.worker.TripReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import java.time.Duration

data class TripUiState(
    val trips: List<Trip> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    fun addTrip(name: String, description: String?, location: String, transport: TransportType, budget: Double, currency: String, startDate: LocalDate, endDate: LocalDate, userId: Int, context: Context) = viewModelScope.launch {
        val trip = Trip(
            name = name,
            description = description,
            location = location,
            transport = transport,
            budget = budget,
            currency = currency,
            startDate = startDate,
            endDate = endDate,
            userId = userId
        )

        val addedTrip = tripRepository.createTrip(trip)

        scheduleTripNotifications(context, addedTrip)
        scheduleTripStatusUpdates(context, addedTrip)

        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    fun loadTrips(userId: Int) = viewModelScope.launch {
        tripRepository.getUserTrips(userId).collect { trips ->
            _uiState.update {
                it.copy(trips = trips)
            }
        }
    }

    fun getTrip(tripId: Int): Flow<Trip?> {
        return tripRepository.getTrip(tripId)
    }

    fun setErrorMessage(errorMessage: String) {
        _uiState.update {
            it.copy(errorMessage = errorMessage)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun cancelTrip(context: Context, tripId: Int) = viewModelScope.launch {
        tripRepository.updateTripStatus(tripId, TripStatus.CANCELLED)
        WorkManager.getInstance(context).cancelUniqueWork("trip_${tripId}_3days")
        WorkManager.getInstance(context).cancelUniqueWork("trip_${tripId}_1day")
        WorkManager.getInstance(context).cancelUniqueWork("trip_${tripId}_startTrip")
        WorkManager.getInstance(context).cancelUniqueWork("trip_${tripId}_endTrip")
    }

    private fun scheduleTripNotifications(context: Context, trip: Trip) {
        val workManager = WorkManager.getInstance(context)

        // Current time
        val now = LocalDateTime.now(ZoneId.systemDefault())

        // Calculate times for notifications
        val startDateTime = trip.startDate.atStartOfDay(ZoneId.systemDefault()).toLocalDateTime()

        // 3 days before
        val threeDaysBefore = startDateTime.minusDays(3)
        if (threeDaysBefore.isAfter(now)) {
            val delayThreeDays = Duration.between(now, threeDaysBefore).toMillis()
            val dataThreeDays = Data.Builder()
                .putInt(TripReminderWorker.KEY_TRIP_ID, trip.id)
                .putString(TripReminderWorker.KEY_TRIP_NAME, trip.name)
                .putInt(TripReminderWorker.KEY_DAYS_BEFORE, 3)
                .build()

            val workThreeDays = OneTimeWorkRequestBuilder<TripReminderWorker>()
                .setInitialDelay(delayThreeDays, TimeUnit.MILLISECONDS)
                .setInputData(dataThreeDays)
                .addTag("trip_${trip.id}_3days")
                .build()

            workManager.enqueueUniqueWork("trip_${trip.id}_3days", ExistingWorkPolicy.REPLACE, workThreeDays)
        }

        // 1 day before
        val oneDayBefore = startDateTime.minusDays(1)
        if (oneDayBefore.isAfter(now)) {
            val delayOneDay = Duration.between(now, oneDayBefore).toMillis()
            val dataOneDay = Data.Builder()
                .putInt(TripReminderWorker.KEY_TRIP_ID, trip.id)
                .putString(TripReminderWorker.KEY_TRIP_NAME, trip.name)
                .putInt(TripReminderWorker.KEY_DAYS_BEFORE, 1)
                .build()

            val workOneDay = OneTimeWorkRequestBuilder<TripReminderWorker>()
                .setInitialDelay(delayOneDay, TimeUnit.MILLISECONDS)
                .setInputData(dataOneDay)
                .addTag("trip_${trip.id}_1day")
                .build()

            workManager.enqueueUniqueWork("trip_${trip.id}_1day", ExistingWorkPolicy.REPLACE, workOneDay)
        }
    }

    private fun scheduleTripStatusUpdates(context: Context, trip: Trip) {
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