package com.example.travelapp.ui.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.api.maps.OTMPoi
import com.example.travelapp.api.maps.PoiCategory
import com.example.travelapp.api.maps.fetchNearbyPOI
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
 *
 * @property context The application context used to initialize location services and the [Geocoder].
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

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
     *
     * This method first verifies that the system location services meet the [Priority.PRIORITY_HIGH_ACCURACY]
     * requirements. If successful, it fetches the coordinate boundary, updates [uiState], and triggers
     * an asynchronous call to retrieve nearby POIs.
     *
     * If system settings need adjustments (e.g., GPS is turned off), a [ResolvableApiException] is captured
     * in the UI state to allow the view layer to resolve it.
     */
    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        if (_uiState.value.currentLocation != null) return

        _uiState.update { it.copy(isFetchingLocation = true) }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        LocationServices.getSettingsClient(context)
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location ->
                    location?.let {
                        val geoPoint = GeoPoint(it.latitude, it.longitude)

                        _uiState.update { state ->
                            state.copy(
                                currentLocation = geoPoint,
                                isFetchingLocation = false
                            )
                        }

                        viewModelScope.launch(Dispatchers.IO) {
                            fetchNearbyPOI(it.latitude, it.longitude) { pois ->
                                _uiState.update { state -> state.copy(pois = pois) }
                            }
                        }
                    } ?: _uiState.update { it.copy(isFetchingLocation = false) }
                }
            }
            .addOnFailureListener { exception ->
                _uiState.update { it.copy(isFetchingLocation = false) }
                if (exception is ResolvableApiException) {
                    _uiState.update { it.copy(resolvableException = exception) }
                }
            }
    }

    /**
     * Uses forward geocoding to resolve a textual description of a place (e.g., "Paris", "Central Park")
     * into spatial coordinates, updates the map center, and fetches nearby POIs for that destination.
     *
     * Performs blocking network operations safely on [Dispatchers.IO].
     *
     * @param location The name or address string of the target destination.
     */
    fun fetchLocationForDestination(location: String) {
        _uiState.update { it.copy(isFetchingLocation = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context)
                val results = geocoder.getFromLocationName(location, 1)
                if (!results.isNullOrEmpty()) {
                    val lat = results[0].latitude
                    val lon = results[0].longitude
                    val geoPoint = GeoPoint(lat, lon)

                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(
                            currentLocation = geoPoint,
                            isFetchingLocation = false
                        )}
                    }

                    fetchNearbyPOI(lat, lon) { pois ->
                        _uiState.update { it.copy(pois = pois) }
                    }
                }
                else {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isFetchingLocation = false) }
                    }
                }
            }
            catch (e: Exception) {
                e.printStackTrace()

                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isFetchingLocation = false) }
                }
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