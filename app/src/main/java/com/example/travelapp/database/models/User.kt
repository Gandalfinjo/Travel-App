package com.example.travelapp.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstname: String,
    val lastname: String,
    val email: String,
    val username: String,
    val password: String
)