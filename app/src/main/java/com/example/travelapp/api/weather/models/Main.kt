package com.example.travelapp.api.weather.models

import com.google.gson.annotations.SerializedName

data class Main(
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    val humidity: Int
)
