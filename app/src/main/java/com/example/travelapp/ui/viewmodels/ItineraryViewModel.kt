package com.example.travelapp.ui.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.api.repositories.LocationRepository
import com.example.travelapp.api.repositories.LocationResult
import com.example.travelapp.database.models.ItineraryItem
import com.example.travelapp.database.repositories.ItineraryRepository
import com.example.travelapp.database.repositories.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.time.LocalDate
import javax.inject.Inject

data class ItineraryUiState(
    val groupedItems: Map<LocalDate, List<ItineraryItem>> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
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
    private val itineraryRepository: ItineraryRepository,
    private val tripRepository: TripRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ItineraryUiState())
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation.asStateFlow()

    private var loadItineraryJob: Job? = null

    fun getTrip(tripId: Int) =
        tripRepository.getTrip(tripId)

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        if (_currentLocation.value != null) return
        viewModelScope.launch {
            try {
                val result = locationRepository.checkSettingsAndGetLocation()
                if (result is LocationResult.Success) {
                    _currentLocation.update { result.geoPoint }
                }
            }
            catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Failed to fetch location") }
            }
        }
    }

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
    fun loadItinerary(tripId: Int) {
        loadItineraryJob?.cancel()
        _uiState.update { it.copy(isLoading = true) }

        loadItineraryJob = viewModelScope.launch {
            itineraryRepository.getItemsForTrip(tripId)
                .catch { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.localizedMessage ?: "Failed to load itinerary")
                    }
                }
                .collect { items ->
                    val sortedGroupedItems = items
                        .groupBy { it.date }
                        .toSortedMap(compareBy { it })
                        .mapValues { entry ->
                            entry.value.sortedBy { item -> item.date }
                        }

                    _uiState.update {
                        it.copy(
                            groupedItems = sortedGroupedItems,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
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
        try {
            itineraryRepository.addItem(item)
        }
        catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Failed to add item") }
        }
    }

    /**
     * Updates an existing itinerary item.
     *
     * Persists changes using [ItineraryRepository].
     *
     * @param item [ItineraryItem] with updated data
     */
    fun updateItem(item: ItineraryItem) = viewModelScope.launch {
        try {
            itineraryRepository.updateItem(item)
        }
        catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Failed to update item") }
        }
    }

    /**
     * Deletes an itinerary item.
     *
     * Removes the item from persistence using [ItineraryRepository].
     *
     * @param item [ItineraryItem] to be deleted
     */
    fun deleteItem(item: ItineraryItem) = viewModelScope.launch {
        try {
            itineraryRepository.deleteItem(item)
        }
        catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Failed to delete item") }
        }
    }

    /**
     * Retrieves an itinerary item.
     *
     * Gets the item based on the provided id using [ItineraryRepository].
     *
     * @param itemId ID of the item to retrieve
     */
    fun getItem(itemId: Int) = itineraryRepository.getItemById(itemId)
}