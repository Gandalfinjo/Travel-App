package com.example.travelapp.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.travelapp.database.models.enums.TransportType
import com.example.travelapp.database.models.enums.TripStatus
import java.time.LocalDate

@Entity(
    tableName = "trips",
    indices = [Index("user_id")],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String? = null,
    val location: String,
    val transport: TransportType = TransportType.OTHER,
    val budget: Double,
    val currency: String,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "end_date") val endDate: LocalDate,
    val status: TripStatus = TripStatus.PLANNED,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "user_id") val userId: Int
)
