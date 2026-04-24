package com.example.travelapp.api.maps

import android.os.Handler
import android.os.Looper
import com.example.travelapp.BuildConfig
import org.json.JSONArray
import java.net.URL

/**
 * Represents a Point of Interest (POI) from OpenTripMap API.
 * @property name Name of the point of interest
 * @property lat Latitude coordinate
 * @property lon Longitude coordinate
 * @property kinds Comma-separated categories/types of the POI
 */
data class OTMPoi(
    val name: String,
    val lat: Double,
    val lon: Double,
    val kinds: String
)

/**
 * Fetches nearby points of interest from OpenTripMap API.
 *
 * Makes a network request on a background thread and returns results on the main thread.
 * Search radius is fixed at 2000 meters with maximum rating of 2.
 *
 * @param lat Latitude of the center point
 * @param lon Longitude of the center point
 * @param onResult Callback invoked on the main thread with the list of POIs (empty list on error)
 */
fun fetchNearbyPOI(lat: Double, lon: Double, onResult: (List<OTMPoi>) -> Unit) {
    val apiKey = BuildConfig.OPEN_TRIP_MAP_API_KEY
    val url =
        "https://api.opentripmap.com/0.1/en/places/radius?radius=2000&lon=$lon&lat=$lat&rate=2&format=json&apikey=$apiKey"

    Thread {
        try {
            val result = URL(url).readText()
            val jsonArray = JSONArray(result)
            val pois = mutableListOf<OTMPoi>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val name = obj.optString("name", "Unknown")
                val point = obj.getJSONObject("point")
                val kinds = obj.optString("kinds", "")
                pois.add(
                    OTMPoi(
                        name = name,
                        lat = point.getDouble("lat"),
                        lon = point.getDouble("lon"),
                        kinds = kinds
                    )
                )
            }

            Handler(Looper.getMainLooper()).post {
                onResult(pois)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}

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