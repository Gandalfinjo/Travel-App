package com.example.travelapp.ui.stateholders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.models.ItineraryItem
import com.example.travelapp.database.repositories.ItineraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ItineraryUiState(
    val groupedItems: Map<LocalDate, List<ItineraryItem>> = emptyMap()
)

@HiltViewModel
class ItineraryViewModel @Inject constructor(
    private val itineraryRepository: ItineraryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ItineraryUiState())
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    fun loadItinerary(tripId: Int) = viewModelScope.launch {
        itineraryRepository.getItemsForTrip(tripId).collect { items ->
            val groupedItems = items.groupBy { it.date }
            _uiState.update {
                it.copy(groupedItems = groupedItems)
            }
        }
    }

    fun addItem(item: ItineraryItem) = viewModelScope.launch {
            itineraryRepository.addItem(item)
    }

    fun updateItem(item: ItineraryItem) = viewModelScope.launch {
            itineraryRepository.updateItem(item)
    }

    fun deleteItem(item: ItineraryItem) = viewModelScope.launch {
            itineraryRepository.deleteItem(item)
    }
}