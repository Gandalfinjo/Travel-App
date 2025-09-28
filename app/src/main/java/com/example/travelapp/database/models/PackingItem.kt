package com.example.travelapp.database.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "packing_items",
    foreignKeys = [ForeignKey(
        entity = Trip::class,
        parentColumns = ["id"],
        childColumns = ["tripId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PackingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tripId: Int,
    val name: String,
    val quantity: Int = 1,
    val isPacked: Boolean = false
)
