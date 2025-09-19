package com.example.travelapp.api.weather

import com.example.travelapp.api.weather.models.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

const val BASE_URL = "https://api.openweathermap.org/"

interface WeatherApi {
    @GET("data/2.5/weather")
    suspend fun gerCurrentWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String = "284357caeb44a80d573b10cc99b69b13",
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "en"
    ): WeatherResponse
}