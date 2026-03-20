package com.example.travelapp.api.weather

import com.example.travelapp.api.weather.models.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://api.openweathermap.org/"

/**
 * Retrofit API interface for OpenWeatherMap API.
 *
 * Provides current weather data using metric units (Celsius, m/s).
 */
interface WeatherApi {
    /**
     * Fetches current weather data for a specified city.
     *
     * @param city City name (e.g., "Belgrade", "Budapest")
     * @param apiKey OpenWeatherMap API key (default: pre-configured key)
     * @param units Unit system for measurements (default: "metric" for Celsius)
     * @param lang Language for weather descriptions (default: "en")
     * @return [WeatherResponse] containing temperature, conditions and wind data
     */
    @GET("data/2.5/weather")
    suspend fun gerCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String = "284357caeb44a80d573b10cc99b69b13",
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): WeatherResponse
}