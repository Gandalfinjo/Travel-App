package com.example.travelapp.api.maps

import com.google.gson.annotations.SerializedName

/**
 * Represents a Point of Interest (POI) from OpenTripMap API.
 * @property name Name of the point of interest
 * @property lat Latitude coordinate
 * @property lon Longitude coordinate
 * @property kinds Comma-separated categories/types of the POI
 */
data class OTMPoi(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("kinds") val kinds: String
)

/**
 * Formats POI category strings into human-readable format.
 *
 * Converts underscore-separated categories to title case and limits the number displayed.
 * Example: "historic_places, interesting_places" -> "Historic Places, Interesting Places"
 *
 * @param kinds Comma-separated string of POI categories
 * @param limit Maximum number of categories to return (default: 2)
 * @return Formatted, comma-separated string of categories
 */
fun formatKinds(kinds: String, limit: Int = 2): String {
    return kinds.split(",")
        .map { kind ->
            kind.trim()
                .split("_")
                .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        }
        .take(limit)
        .joinToString(", ")
}

/**
 * Checks if this Point of Interest matches a specific [PoiCategory].
 *
 * This method evaluates whether any of the category's defined tags match or are
 * contained within the comma-separated kinds string of this POI.
 *
 * @param category The [PoiCategory] to check against.
 * @return True if there is a category match, false otherwise.
 */
fun OTMPoi.matchesCategory(category: PoiCategory): Boolean {
    if (this.kinds.isEmpty()) return false

    return category.kinds.any { categoryKind ->
        this.kinds.contains(categoryKind, ignoreCase = true)
    }
}