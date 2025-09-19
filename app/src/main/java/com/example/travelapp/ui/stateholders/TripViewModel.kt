package com.example.travelapp.ui.stateholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.models.Trip
import com.example.travelapp.database.models.enums.TransportType
import com.example.travelapp.database.repositories.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

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

    fun addTrip(name: String, description: String?, location: String, transport: TransportType, budget: Double, currency: String, startDate: LocalDate, endDate: LocalDate, userId: Int) = viewModelScope.launch {
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

        tripRepository.createTrip(trip)

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
}