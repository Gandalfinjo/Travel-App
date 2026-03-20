package com.example.travelapp.api.weather.models

/**
 * Weather data response from OpenWeatherMap API.
 * Temperatures are in Celsius, wind speed in m/s.
 */
data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind
)
