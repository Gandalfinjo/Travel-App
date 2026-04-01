package com.example.travelapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.models.CategoryTotal
import com.example.travelapp.database.models.Trip
import com.example.travelapp.database.models.enums.TripStatus
import com.example.travelapp.database.repositories.ExpenseRepository
import com.example.travelapp.database.repositories.TripRepository
import com.example.travelapp.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Data class that combines the [Trip] with the total expanses of that trip
 */
data class TripWithExpenses(
    val trip: Trip,
    val totalSpent: Double
)

data class StatisticsUiState(
    val totalTrips: Int = 0,
    val tripsByStatus: Map<TripStatus, Int> = emptyMap(),
    val averageDuration: Double = 0.0,
    val topDestination: String? = null,
    val topSpendingTrips: List<TripWithExpenses> = emptyList(),
    val totalByCategory: List<CategoryTotal> = emptyList(),
    val isLoading: Boolean = true
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
 * Coordinates with [TripRepository] and [ExpenseRepository] to retrieve trip data.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val expenseRepository: ExpenseRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.loggedInUserId.first()?.let {
                loadStatistics(it)
            }
        }
    }

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
        _uiState.update { it.copy(isLoading = true) }

        launch {
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
                    .map { trip ->
                        TripWithExpenses(
                            trip = trip,
                            totalSpent = expenseRepository.getTotalSpentForTrip(trip.id)
                        )
                    }
                    .filter { it.totalSpent > 0 }
                    .sortedByDescending { it.totalSpent }
                    .take(5)

                _uiState.update {
                    it.copy(
                        totalTrips = trips.size,
                        tripsByStatus = byStatus,
                        averageDuration = averageDuration,
                        topDestination = topDestination,
                        topSpendingTrips = topSpendingTrips,
                        isLoading = false
                    )
                }
            }
        }

        launch {
            expenseRepository.getTotalByCategoryForUser(userId).collect { totals ->
                _uiState.update { it.copy(totalByCategory = totals) }
            }
        }
    }
}