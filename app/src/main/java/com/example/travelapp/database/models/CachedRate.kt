package com.example.travelapp.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_rates")
data class CachedRate(
    @PrimaryKey val currencyPair: String,
    val rate: Double,
    val timestamp: Long
)
