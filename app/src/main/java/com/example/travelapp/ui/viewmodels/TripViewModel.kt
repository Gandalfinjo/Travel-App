package com.example.travelapp.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.travelapp.R
import com.example.travelapp.database.converters.TravelTypeConverters
import com.example.travelapp.database.models.Trip
import com.example.travelapp.database.models.enums.TransportType
import com.example.travelapp.database.models.enums.TripStatus
import com.example.travelapp.database.repositories.TripRepository
import com.example.travelapp.schedulers.TripScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripUiState(
    val trips: List<Trip> = emptyList(),
    val errorMessage: String? = null,
    val tripAddedSuccessfully: Boolean = false,
)

@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val tripScheduler: TripScheduler
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    fun addTrip(
        name: String,
        description: String?,
        location: String,
        transport: TransportType,
        budget: String,
        currency: String,
        startDateMillis: Long?,
        endDateMillis: Long?,
        userId: Int,
        context: Context
    ) = viewModelScope.launch {
        // Validation
        if (name.isBlank() || location.isBlank() ||
            budget.isBlank() || currency.isBlank() ||
            startDateMillis == null || endDateMillis == null) {
            _uiState.update {
                it.copy(errorMessage = context.getString(R.string.missing_fields))
            }
            return@launch
        }

        if (startDateMillis > endDateMillis) {
            _uiState.update {
                it.copy(errorMessage = context.getString(R.string.you_cannot_put_the_start_date_after_the_end_date))
            }
            return@launch
        }

        // Convert to LocalDate
        val startDate = TravelTypeConverters().fromTimestampMillis(startDateMillis)!!
        val endDate = TravelTypeConverters().fromTimestampMillis(endDateMillis)!!

        val trip = Trip(
            name = name,
            description = description,
            location = location,
            transport = transport,
            budget = budget.toDoubleOrNull() ?: 0.0,
            currency = currency,
            startDate = startDate,
            endDate = endDate,
            userId = userId
        )

        val addedTrip = tripRepository.createTrip(trip)

        tripScheduler.scheduleTripNotifications(addedTrip)
        tripScheduler.scheduleTripStatusUpdates(addedTrip)

        _uiState.update {
            it.copy(
                errorMessage = null,
                tripAddedSuccessfully = true
            )
        }
    }

    fun resetAddTripState() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                tripAddedSuccessfully = false
            )
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

}