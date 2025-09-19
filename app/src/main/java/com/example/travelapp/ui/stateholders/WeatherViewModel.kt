package com.example.travelapp.ui.stateholders

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

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(WeatherState(isLoading = true))
    val uiState: StateFlow<WeatherState> = _uiState

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
                it.copy(error = e.message)
            }
        }
    }
}