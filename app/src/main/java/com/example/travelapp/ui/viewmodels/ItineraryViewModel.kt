package com.example.travelapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.models.ItineraryItem
import com.example.travelapp.database.repositories.ItineraryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ItineraryUiState(
    val groupedItems: Map<LocalDate, List<ItineraryItem>> = emptyMap()
)

/**
 * ViewModel responsible for managing itinerary items for a specific trip.
 *
 * Handles loading, grouping, and CRUD operations for itinerary items.
 * Groups items by date to simplify UI rendering (e.g. day-by-day itinerary view).
 *
 * Coordinates with [ItineraryRepository] for data persistence.
 */
@HiltViewModel
class ItineraryViewModel @Inject constructor(
    private val itineraryRepository: ItineraryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ItineraryUiState())
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    /**
     * Loads all itinerary items for the specified trip and groups them by date.
     *
     * The resulting map is structured as:
     * - Key: [LocalDate] representing the day
     * - Value: List of [ItineraryItem] for that day
     *
     * Updates [uiState] with grouped items for easier UI display.
     *
     * @param tripId ID of the trip whose itinerary should be loaded
     */
    fun loadItinerary(tripId: Int) = viewModelScope.launch {
        itineraryRepository.getItemsForTrip(tripId).collect { items ->
            val groupedItems = items.groupBy { it.date }
            _uiState.update {
                it.copy(groupedItems = groupedItems)
            }
        }
    }

    /**
     * Adds a new itinerary item.
     *
     * Persists the item using [ItineraryRepository].
     *
     * @param item [ItineraryItem] to be added
     */
    fun addItem(item: ItineraryItem) = viewModelScope.launch {
            itineraryRepository.addItem(item)
    }

    /**
     * Updates an existing itinerary item.
     *
     * Persists changes using [ItineraryRepository].
     *
     * @param item [ItineraryItem] with updated data
     */
    fun updateItem(item: ItineraryItem) = viewModelScope.launch {
            itineraryRepository.updateItem(item)
    }

    /**
     * Deletes an itinerary item.
     *
     * Removes the item from persistence using [ItineraryRepository].
     *
     * @param item [ItineraryItem] to be deleted
     */
    fun deleteItem(item: ItineraryItem) = viewModelScope.launch {
            itineraryRepository.deleteItem(item)
    }
}