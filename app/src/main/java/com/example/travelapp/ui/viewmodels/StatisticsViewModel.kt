package com.example.travelapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.models.Trip
import com.example.travelapp.database.models.enums.TripStatus
import com.example.travelapp.database.repositories.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class StatisticsUiState(
    val totalTrips: Int = 0,
    val tripsByStatus: Map<TripStatus, Int> = emptyMap(),
    val averageDuration: Double = 0.0,
    val topDestination: String? = null,
    val topSpendingTrips: List<Trip> = emptyList()
)

/**
 * ViewModel responsible for calculating and exposing travel statistics.
 *
 * Aggregates data from user's trips to provide insights such as:
 * - Total number of trips
 * - Distribution of trips by status
 * - Average trip duration
 * - Most frequently visited destination
 * - Top spending trips
 *
 * Coordinates with [TripRepository] to retrieve trip data.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    /**
     * Loads and calculates statistics for the specified user.
     *
     * Performs the following calculations:
     * - Counts total number of trips
     * - Groups trips by [TripStatus]
     * - Calculates average duration in days
     * - Determines most visited destination
     * - Selects top 5 trips by highest budget
     *
     * If the user has no trips, resets the state to default values.
     *
     * @param userId ID of the user whose statistics should be calculated
     */
    fun loadStatistics(userId: Int) = viewModelScope.launch {
        tripRepository.getUserTrips(userId).collect { trips ->
            if (trips.isEmpty()) {
                _uiState.value = StatisticsUiState()
                return@collect
            }

            val byStatus = trips.groupingBy { it.status }.eachCount()
            val averageDuration = trips
                .map { ChronoUnit.DAYS.between(it.startDate, it.endDate).toDouble() }
                .average()

            val topDestination = trips
                .groupingBy { it.location }
                .eachCount()
                .maxByOrNull { it.value }
                ?.key

            val topSpendingTrips = trips
                .sortedByDescending { it.budget }
                .take(5)

            _uiState.update {
                it.copy(
                    totalTrips = trips.size,
                    tripsByStatus = byStatus,
                    averageDuration = averageDuration,
                    topDestination = topDestination,
                    topSpendingTrips = topSpendingTrips
                )
            }
        }
    }
}