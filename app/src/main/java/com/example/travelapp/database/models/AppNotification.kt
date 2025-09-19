package com.example.travelapp.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.travelapp.database.models.enums.NotificationType

@Entity(
    tableName = "notifications",
    indices = [Index("trip_id")],
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: NotificationType,
    val date: Long,
    @ColumnInfo(name = "trip_id") val tripId: Int
)
