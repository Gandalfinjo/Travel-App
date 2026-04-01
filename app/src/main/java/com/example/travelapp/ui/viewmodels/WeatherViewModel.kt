package com.example.travelapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.api.repositories.WeatherRepository
import com.example.travelapp.api.weather.models.WeatherResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherState(
    val isLoading: Boolean = false,
    val weather: WeatherResponse? = null,
    val error: String? = null
)

/**
 * ViewModel responsible for fetching and managing current weather data.
 *
 * Retrieves weather information for a given city and exposes it to the UI.
 * Handles loading and error states during API calls.
 *
 * Coordinates with [WeatherRepository] for remote weather data fetching.
 */
@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(WeatherState(isLoading = true))
    val uiState: StateFlow<WeatherState> = _uiState

    /**
     * Loads current weather data for the specified city.
     *
     * Performs the following:
     * - Sets loading state to true
     * - Fetches weather data from API
     * - Updates UI state with result or error
     *
     * On success, updates [WeatherState.weather].
     * On failure, sets [WeatherState.error] with exception message.
     *
     * @param city Name of the city for which weather should be retrieved
     */
    fun loadCurrentWeather(city: String) = viewModelScope.launch {
        _uiState.update {
            it.copy(isLoading = true)
        }

        try {
            val weatherResponse = weatherRepository.getCurrentWeather(city)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    weather = weatherResponse,
                    error = null
                )
            }
        }
        catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}