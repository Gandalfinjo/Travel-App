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
import com.example.travelapp.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripUiState(
    val trips: List<Trip> = emptyList(),
    val errorMessage: String? = null,
    val tripAddedSuccessfully: Boolean = false,
    val isLoading: Boolean = true
)

/**
 * ViewModel responsible for managing trip-related UI state and business logic.
 *
 * Handles trip creation, validation, loading and cancellation
 * Coordinates with [TripRepository] for data persistence and [TripScheduler]
 * for background task scheduling.
 */
@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val tripScheduler: TripScheduler,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.loggedInUserId.first()?.let {
                loadTrips(it)
            }
        }
    }

    /**
     * Validates user input and creates a new trip if all validations pass.
     *
     * Performs the following validations:
     * - All required fields are filled
     * - Start date is before end date
     * - Budged is a valid number
     *
     * On success, schedules notifications and status updates for the trip.
     *
     * @param name Trip name
     * @param description Optional trip description
     * @param location Trip destination
     * @param transport Type of transportation
     * @param budget Budget as String (will be converted to Double)
     * @param currency Currency code (e.g., "USD", "EUR")
     * @param startDateMillis Start date in milliseconds since epoch
     * @param endDateMillis End date in milliseconds since epoch
     * @param userId ID of the user creating the trip
     * @param context Android context for accessing string resources
     */
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

    /**
     * Resets the add trip state flags.
     * Should be called after successfully navigating away from [com.example.travelapp.ui.screens.AddTripScreen].
     */
    fun resetAddTripState() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                tripAddedSuccessfully = false
            )
        }
    }

    /**
     * Loads all trips for the specified (logged in) user.
     * Updates [uiState] with the list of trips.
     *
     * @param userId ID of the user whose trips to load
     */
    fun loadTrips(userId: Int) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        tripRepository.getUserTrips(userId).collect { trips ->
            _uiState.update {
                it.copy(
                    trips = trips,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Retrieves a single trip by ID as a Flow.
     *
     * @param tripId ID of the trip to retrieve
     * @return Flow emitting the trip or null if not found
     */
    fun getTrip(tripId: Int): Flow<Trip?> {
        return tripRepository.getTrip(tripId)
    }

    /**
     * Clears any error message from the UI state.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Cancels a trip and removes all scheduled notifications and status updates.
     *
     * @param context Android context for accessing WorkManager
     * @param tripId ID of the trip to cancel
     */
    fun cancelTrip(context: Context, tripId: Int) = viewModelScope.launch {
        tripRepository.updateTripStatus(tripId, TripStatus.CANCELLED)
        WorkManager.getInstance(context).cancelUniqueWork("trip_${tripId}_3days")
        WorkManager.getInstance(context).cancelUniqueWork("trip_${tripId}_1day")
        WorkManager.getInstance(context).cancelUniqueWork("trip_${tripId}_startTrip")
        WorkManager.getInstance(context).cancelUniqueWork("trip_${tripId}_endTrip")
    }

}