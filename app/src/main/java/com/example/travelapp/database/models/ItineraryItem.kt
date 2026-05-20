package com.example.travelapp.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "itinerary_items",
    foreignKeys = [ForeignKey(
        entity = Trip::class,
        parentColumns = ["id"],
        childColumns = ["tripId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ItineraryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val date: LocalDate,
    val title: String,
    val description: String? = null,
    val isDone: Boolean = false,
    @ColumnInfo(name = "image_path") val imagePath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
