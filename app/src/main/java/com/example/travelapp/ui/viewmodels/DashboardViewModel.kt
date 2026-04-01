package com.example.travelapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.models.ItineraryItem
import com.example.travelapp.database.models.Trip
import com.example.travelapp.database.models.enums.TripStatus
import com.example.travelapp.database.repositories.ExpenseRepository
import com.example.travelapp.database.repositories.ItineraryRepository
import com.example.travelapp.database.repositories.PackingRepository
import com.example.travelapp.database.repositories.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class DashboardUiState(
    val activeTrip: Trip? = null,
    val upcomingTrip: Trip? = null,
    val todayItinerary: List<ItineraryItem> = emptyList(),
    val totalSpentOnActiveTrip: Double = 0.0,
    val activeTripPackingProgress: Pair<Int, Int> = Pair(0, 0),
    val upcomingTripPackingProgress: Pair<Int, Int> = Pair(0, 0),
    val totalTrips: Int = 0,
    val uniqueDestinations: Int = 0,
    val isLoading: Boolean = false
)

/**
 * ViewModel for the Dashboard Screen
 *
 * Handles loading the Dashboard data using [TripRepository], [ItineraryRepository], [ExpenseRepository] and [PackingRepository]
 *
 * @param tripRepository Repository for retrieving trip information
 * @param itineraryRepository Repository for retrieving itinerary information
 * @param expenseRepository Repository for retrieving expense information
 * @param packingRepository Repository for retrieving packing infomration
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val itineraryRepository: ItineraryRepository,
    private val expenseRepository: ExpenseRepository,
    private val packingRepository: PackingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /**
     * Loads data for the logged-in user about ongoing trip, upcoming trip, short overview of all trips, today's itinerary
     *
     * @param userId ID of the user for which to show data
     */
    fun loadDashboard(userId: Int) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        launch {
            tripRepository.getUserTrips(userId).collect { trips ->
                val activeTrip = trips.firstOrNull { it.status == TripStatus.ONGOING }

                val upcomingTrip = trips
                    .filter { it.status == TripStatus.PLANNED }
                    .sortedBy { it.startDate }
                    .firstOrNull {
                        val daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), it.startDate)
                        daysUntil in 0..7
                    }

                _uiState.update {
                    it.copy(
                        activeTrip = activeTrip,
                        upcomingTrip = upcomingTrip,
                        totalTrips = trips.size,
                        uniqueDestinations = trips.map { t -> t.location }.toSet().size,
                        isLoading = false
                    )
                }

                activeTrip?.let { trip ->
                    launch {
                        itineraryRepository.getItemsForTrip(trip.id).collect { items ->
                            val todayItems = items.filter { it.date == LocalDate.now() }

                            _uiState.update { it.copy(todayItinerary = todayItems) }
                        }
                    }

                    launch {
                        expenseRepository.getTotalByTrip(trip.id).collect { total ->
                            _uiState.update { it.copy(totalSpentOnActiveTrip = total ?: 0.0) }
                        }
                    }

                    launch {
                        packingRepository.getItemsForTrip(trip.id).collect { items ->
                            val packed = items.count { it.isPacked }
                            val total = items.size

                            _uiState.update { it.copy(activeTripPackingProgress = Pair(packed, total)) }
                        }
                    }
                }

                upcomingTrip?.let { trip ->
                    launch {
                        packingRepository.getItemsForTrip(trip.id).collect { items ->
                            val packed = items.count { it.isPacked }
                            val total = items.size

                            _uiState.update { it.copy(upcomingTripPackingProgress = Pair(packed, total)) }
                        }
                    }
                }
            }
        }
    }
}