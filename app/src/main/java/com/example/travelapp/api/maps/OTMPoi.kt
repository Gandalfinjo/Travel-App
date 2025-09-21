package com.example.travelapp.api.maps

import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import java.net.URL

data class OTMPoi(
    val name: String,
    val lat: Double,
    val lon: Double,
    val kinds: String
)

fun fetchNearbyPOI(lat: Double, lon: Double, onResult: (List<OTMPoi>) -> Unit) {
    val url =
        "https://api.opentripmap.com/0.1/en/places/radius?radius=1000&lon=$lon&lat=$lat&rate=2&format=json&apikey=5ae2e3f221c38a28845f05b676041a1841e510d13dcdbd3fd0a12c9c"

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
