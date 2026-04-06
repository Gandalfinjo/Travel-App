package com.example.travelapp.ui.elements

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.travelapp.database.models.enums.TripStatus

@Composable
fun tripStatusStyle(status: TripStatus): Triple<Color, Color, ImageVector> {
    return when (status) {
        TripStatus.PLANNED -> Triple(Color(0xFF185FA5), Color(0xFFE6F1FB), Icons.Default.Schedule)
        TripStatus.ONGOING -> Triple(Color(0xFF0F6E56), Color(0xFFE1F5EE), Icons.Default.FlightTakeoff)
        TripStatus.FINISHED -> Triple(Color(0xFF6B4E00), Color(0xFFFAEEDA), Icons.Default.CheckCircle)
        TripStatus.CANCELLED -> Triple(Color(0xFF9B1C1C), Color(0xFFFFEBEB), Icons.Default.Cancel)
    }
}