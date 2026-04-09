package com.example.travelapp.database.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.travelapp.database.models.enums.ExpenseCategory
import java.time.LocalDate

@Entity(
    tableName = "expenses",
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
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "trip_id") val tripId: Int,
    val amount: Double,
    val currency: String,
    @ColumnInfo(name = "amount_in_trip_currency") val amountInTripCurrency: Double,
    @ColumnInfo(name = "amount_in_default_currency") val amountInDefaultCurrency: Double,
    val category: ExpenseCategory,
    val description: String? = null,
    val date: LocalDate?,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
