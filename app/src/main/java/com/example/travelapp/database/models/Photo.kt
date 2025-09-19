package com.example.travelapp.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Place::class,
            parentColumns = ["id"],
            childColumns = ["place_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "file_path") val filePath: String,
    @ColumnInfo(name = "date_taken") val dateTaken: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @ColumnInfo(name = "trip_id") val tripId: Int,
    @ColumnInfo(name = "place_id") val placeId: Int? = null
)
