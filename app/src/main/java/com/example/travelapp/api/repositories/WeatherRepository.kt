package com.example.travelapp.api.repositories

import com.example.travelapp.api.weather.WeatherApi
import com.example.travelapp.api.weather.models.WeatherResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for fetching weather data from OpenWeatherMap API.
 *
 * Provides a clean interface for weather-related operations,
 * abstracting the underlying API implementation.
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApi
) {
    /**
     * Retrieves current weather information for a specified city.
     *
     * @param city Name of the City (e.g., "Belgrade", "Budapest")
     * @return [WeatherResponse] containing temperature, conditions, humidity and wind data.
     */
    suspend fun getCurrentWeather(city: String): WeatherResponse {
        return weatherApi.gerCurrentWeather(city)
    }
}