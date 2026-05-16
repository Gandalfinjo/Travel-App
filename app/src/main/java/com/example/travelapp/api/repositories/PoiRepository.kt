package com.example.travelapp.api.repositories

import com.example.travelapp.BuildConfig
import com.example.travelapp.api.maps.OTMPoi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository responsible for fetching and processing Point of Interest (POI) data
 * from remote data sources.
 *
 * This repository abstracts the underlying network operations and JSON parsing,
 * providing a clean, coroutine-safe API for business logic components.
 */
@Singleton
class PoiRepository @Inject constructor() {
    /**
     * Fetches points of interest within a fixed 2000-meter radius around the specified coordinates.
     *
     * This function executes a blocking network request via the OpenTripMap API on the
     * [Dispatchers.IO] thread pool to ensure the calling thread is not blocked.
     * Results are restricted to a minimum popularity rating of 2.
     *
     * @param lat The latitude coordinate of the center search point.
     * @param lon The longitude coordinate of the center search point.
     * @return A list of [OTMPoi] elements matching the criteria, or an empty list if
     * the network request fails, times out, or encounters parsing errors.
     */
    suspend fun getNearbyPois(lat: Double, lon: Double): List<OTMPoi> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.OPEN_TRIP_MAP_API_KEY
        val url = "https://api.opentripmap.com/0.1/en/places/radius?radius=2000&lon=$lon&lat=$lat&rate=2&format=json&apikey=$apiKey"

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
                    OTMPoi(name = name, lat = point.getDouble("lat"), lon = point.getDouble("lon"), kinds = kinds)
                )
            }

            pois
        }
        catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}