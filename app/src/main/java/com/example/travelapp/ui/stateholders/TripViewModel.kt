package com.example.travelapp.ui.stateholders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.travelapp.database.models.Trip
import com.example.travelapp.database.models.enums.TransportType
import com.example.travelapp.database.repositories.TripRepository
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

}