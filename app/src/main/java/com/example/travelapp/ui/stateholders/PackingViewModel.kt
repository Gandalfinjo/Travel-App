package com.example.travelapp.ui.stateholders

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

@HiltViewModel
class PackingViewModel @Inject constructor(
    private val packingRepository: PackingRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PackingUiState())
    val uiState: StateFlow<PackingUiState> = _uiState.asStateFlow()

    fun loadPackingItems(tripId: Int) = viewModelScope.launch {
        packingRepository.getItemsForTrip(tripId).collect { items ->
            _uiState.update {
                it.copy(items = items)
            }
        }
    }

    fun addItem(item: PackingItem) = viewModelScope.launch {
        packingRepository.addItem(item)
    }

    fun updateItem(item: PackingItem) = viewModelScope.launch {
        packingRepository.updateItem(item)
    }

    fun deleteItem(item: PackingItem) = viewModelScope.launch {
        packingRepository.deleteItem(item)
    }
}