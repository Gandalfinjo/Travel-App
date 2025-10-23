package com.example.travelapp.ui.stateholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val topDestination: String? = null
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

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

            _uiState.update {
                it.copy(
                    totalTrips = trips.size,
                    tripsByStatus = byStatus,
                    averageDuration = averageDuration,
                    topDestination = topDestination
                )
            }
        }
    }
}