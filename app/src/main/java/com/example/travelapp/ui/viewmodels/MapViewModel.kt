package com.example.travelapp.ui.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.api.maps.OTMPoi
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
    val resolvableException: ResolvableApiException? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun onPermissionGranted() {
        _uiState.update { it.copy(isPermissionGranted = true) }
    }

    fun clearResolvableException() {
        _uiState.update { it.copy(resolvableException = null) }
    }

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
}