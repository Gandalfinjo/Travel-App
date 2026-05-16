package com.example.travelapp.ui.viewmodels

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.api.maps.OTMPoi
import com.example.travelapp.api.maps.PoiCategory
import com.example.travelapp.api.repositories.LocationRepository
import com.example.travelapp.api.repositories.LocationResult
import com.example.travelapp.api.repositories.PoiRepository
import com.google.android.gms.common.api.ResolvableApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

data class MapUiState(
    val currentLocation: GeoPoint? = null,
    val pois: List<OTMPoi> = emptyList(),
    val isFetchingLocation: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val resolvableException: ResolvableApiException? = null,
    val selectedCategories: Set<PoiCategory> = emptySet()
)

/**
 * ViewModel responsible for managing map-related data and user interactions.
 *
 * Handles device location retrieval via Google Play Services, text-based forward geocoding,
 * and delegates the fetching of nearby Points of Interest (POIs).
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val poiRepository: PoiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    /**
     * Updates the UI state to reflect that location permissions have been successfully granted.
     */
    fun onPermissionGranted() {
        _uiState.update { it.copy(isPermissionGranted = true) }
    }

    /**
     * Clears any active [ResolvableApiException] from the UI state once it has been handled or dismissed.
     */
    fun clearResolvableException() {
        _uiState.update { it.copy(resolvableException = null) }
    }

    /**
     * Requests the physical device's current location.
     */
    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        if (_uiState.value.currentLocation != null) return

        _uiState.update { it.copy(isFetchingLocation = true) }

        viewModelScope.launch {
            when (val result = locationRepository.checkSettingsAndGetLocation()) {
                is LocationResult.Success -> {
                    _uiState.update { state ->
                        state.copy(currentLocation = result.geoPoint, isFetchingLocation = false)
                    }

                    val fetchedPois = poiRepository.getNearbyPois(result.geoPoint.latitude, result.geoPoint.longitude)

                    _uiState.update { it.copy(pois = fetchedPois) }
                }

                is LocationResult.ResolutionRequired -> {
                    _uiState.update { it.copy(resolvableException = result.exception, isFetchingLocation = false) }
                }

                is LocationResult.Failure -> {
                    _uiState.update { it.copy(isFetchingLocation = false) }
                }
            }
        }
    }

    /**
     * Resolves text search input into matching POI coordinates.
     */
    fun fetchLocationForDestination(location: String) {
        _uiState.update { it.copy(isFetchingLocation = true) }

        viewModelScope.launch {
            val geoPoint = locationRepository.getCoordinatesFromAddress(location)

            if (geoPoint != null) {
                val fetchedPois = poiRepository.getNearbyPois(geoPoint.latitude, geoPoint.longitude)

                _uiState.update { state ->
                    state.copy(currentLocation = geoPoint, pois = fetchedPois, isFetchingLocation = false)
                }
            }
            else {
                _uiState.update { it.copy(isFetchingLocation = false) }
            }
        }
    }

    /**
     * Toggles the selection state of a [PoiCategory] filter.
     *
     * Adds the category to the filter list if it isn't already present, or removes it if it is.
     *
     * @param category The [PoiCategory] targeted for toggling.
     */
    fun toggleCategory(category: PoiCategory) {
        _uiState.update { state ->
            val updated = if (category in state.selectedCategories)
                state.selectedCategories - category
            else
                state.selectedCategories + category
            state.copy(selectedCategories = updated)
        }
    }
}