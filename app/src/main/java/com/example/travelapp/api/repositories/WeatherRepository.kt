package com.example.travelapp.api.repositories

import com.example.travelapp.api.weather.WeatherApi
import com.example.travelapp.api.weather.models.WeatherResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApi
) {
    suspend fun getCurrentWeather(city: String): WeatherResponse {
        return weatherApi.gerCurrentWeather(city)
    }
}