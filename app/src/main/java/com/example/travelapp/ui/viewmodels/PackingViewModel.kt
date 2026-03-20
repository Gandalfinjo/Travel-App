package com.example.travelapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.models.PackingItem
import com.example.travelapp.database.repositories.PackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PackingUiState(
    val items: List<PackingItem> = emptyList()
)

/**
 * ViewModel responsible for managing packing list items for a specific trip.
 *
 * Handles loading and CRUD operations for packing items.
 * Maintains a simple flat list of items used for packing preparation.
 *
 * Coordinates with [PackingRepository] for data persistence.
 */
@HiltViewModel
class PackingViewModel @Inject constructor(
    private val packingRepository: PackingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PackingUiState())
    val uiState: StateFlow<PackingUiState> = _uiState.asStateFlow()

    /**
     * Loads all packing items for the specified trip.
     *
     * Updates [uiState] with the latest list of items.
     *
     * @param tripId ID of the trip whose packing items should be loaded
     */
    fun loadPackingItems(tripId: Int) = viewModelScope.launch {
        packingRepository.getItemsForTrip(tripId).collect { items ->
            _uiState.update {
                it.copy(items = items)
            }
        }
    }

    /**
     * Adds a new packing item.
     *
     * Persists the item using [PackingRepository].
     *
     * @param item [PackingItem] to be added
     */
    fun addItem(item: PackingItem) = viewModelScope.launch {
        packingRepository.addItem(item)
    }

    /**
     * Updates an existing packing item.
     *
     * Persists changes using [PackingRepository].
     *
     * @param item [PackingItem] with updated data
     */
    fun updateItem(item: PackingItem) = viewModelScope.launch {
        packingRepository.updateItem(item)
    }

    /**
     * Deletes a packing item.
     *
     * Removes the item from persistence using [PackingRepository].
     *
     * @param item [PackingItem] to be deleted
     */
    fun deleteItem(item: PackingItem) = viewModelScope.launch {
        packingRepository.deleteItem(item)
    }
}