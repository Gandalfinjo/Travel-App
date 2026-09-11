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
import com.example.travelapp.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    val isLoading: Boolean = true
)

/**
 * ViewModel for the Dashboard Screen
 *
 * Handles loading the Dashboard data using [TripRepository], [ItineraryRepository], [ExpenseRepository] and [PackingRepository]
 *
 * @param tripRepository Repository for retrieving trip information
 * @param itineraryRepository Repository for retrieving itinerary information
 * @param expenseRepository Repository for retrieving expense information
 * @param packingRepository Repository for retrieving packing information
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val itineraryRepository: ItineraryRepository,
    private val expenseRepository: ExpenseRepository,
    private val packingRepository: PackingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeDashboardData()
    }

    /**
     * Loads data for the logged-in user about ongoing trip, upcoming trip, short overview of all trips, today's itinerary
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDashboardData() = viewModelScope.launch {
        sessionManager.loggedInUserId.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(DashboardUiState(isLoading = false))
            }
            else {
                tripRepository.getUserTrips(userId).flatMapLatest { trips ->
                    val today = LocalDate.now()
                    val activeTrip = trips.firstOrNull { it.status == TripStatus.ONGOING }
                    val upcomingTrip = trips
                        .filter { it.status == TripStatus.PLANNED }
                        .sortedBy { it.startDate }
                        .firstOrNull {
                            val daysUntil = ChronoUnit.DAYS.between(today, it.startDate)
                            daysUntil in 0..7
                        }

                    val totalTrips = trips.size
                    val uniqueDestinations = trips.map { it.location }.toSet().size

                    val activeItineraryFlow = activeTrip?.let {
                        itineraryRepository.getItemsForTrip(it.id)
                    } ?: flowOf(emptyList())

                    val activeExpensesFlow = activeTrip?.let {
                        expenseRepository.getTotalByTrip(it.id)
                    } ?: flowOf(0.0)

                    val activePackingFlow = activeTrip?.let {
                        packingRepository.getItemsForTrip(it.id)
                    } ?: flowOf(emptyList())

                    val upcomingPackingFlow = upcomingTrip?.let {
                        packingRepository.getItemsForTrip(it.id)
                    } ?: flowOf(emptyList())

                    combine(
                        activeItineraryFlow,
                        activeExpensesFlow,
                        activePackingFlow,
                        upcomingPackingFlow
                    ) { itinerary, expenseTotal, activePacking, upcomingPacking ->
                        val todayItems = itinerary.filter { it.date == today }
                        val activePackedCount = activePacking.count { it.isPacked }
                        val upcomingPackedCount = upcomingPacking.count { it.isPacked }

                        DashboardUiState(
                            activeTrip = activeTrip,
                            upcomingTrip = upcomingTrip,
                            todayItinerary = todayItems,
                            totalSpentOnActiveTrip = expenseTotal ?: 0.0,
                            activeTripPackingProgress = Pair(activePackedCount, activePacking.size),
                            upcomingTripPackingProgress = Pair(upcomingPackedCount, upcomingPacking.size),
                            totalTrips = totalTrips,
                            uniqueDestinations = uniqueDestinations,
                            isLoading = false
                        )
                    }
                }
            }
        }.collect { state ->
            _uiState.value = state
        }
    }
}