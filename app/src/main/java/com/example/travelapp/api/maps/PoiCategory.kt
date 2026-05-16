package com.example.travelapp.api.maps

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.ui.graphics.vector.ImageVector

enum class PoiCategory(
    val label: String,
    val kinds: List<String>,
    val icon: ImageVector
) {
    ARCHITECTURE(
        label = "Architecture",
        kinds = listOf("architecture", "historic_architecture", "other_buildings_and_structures"),
        icon = Icons.Default.AccountBalance
    ),
    MONUMENTS(
        label = "Monuments",
        kinds = listOf("monuments", "monuments_and_memorials", "sculptures", "fountains"),
        icon = Icons.Default.NearMe
    ),
    MUSEUMS(
        label = "Museums",
        kinds = listOf("museums", "art_galleries"),
        icon = Icons.Default.Museum
    ),
    RELIGION(
        label = "Religion",
        kinds = listOf("religion", "churches", "cathedrals", "synagogues", "other_churches", "other_temples"),
        icon = Icons.Default.Church
    ),
    PARKS(
        label = "Parks",
        kinds = listOf("gardens_and_parks", "urban_environment"),
        icon = Icons.Default.Park
    ),
    CULTURE(
        label = "Culture",
        kinds = listOf("theatres_and_entertainments", "cinemas", "cultural"),
        icon = Icons.Default.TheaterComedy
    ),
    HISTORIC(
        label = "Historic",
        kinds = listOf("historic", "battlefields", "historical_places", "archaeology"),
        icon = Icons.Default.History
    ),
    OTHER(
        label = "Other",
        kinds = listOf("bridges", "railway_stations", "industrial_facilities", "cemeteries"),
        icon = Icons.Default.Category
    )
}