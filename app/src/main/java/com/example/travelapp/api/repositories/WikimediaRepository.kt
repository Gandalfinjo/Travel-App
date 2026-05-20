package com.example.travelapp.api.repositories

import android.content.Context
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

class WikimediaRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    /**
     * Searches Wikimedia for a page matching the query, then fetches:
     * - The main image URL
     * - Coordinates (if available)
     * Downloads the image and saves it to internal storage.
     *
     * @param query Search term (e.g. itinerary item title + trip location)
     * @return Pair of (local image file path, GeoPoint) — either can be null if unavailable
     */
    suspend fun fetchImageAndCoordinates(query: String): Pair<String?, Pair<Double, Double>?> = withContext(Dispatchers.IO) {
        try {
            val searchUrl = "https://en.wikipedia.org/w/api.php" +
                    "?action=query&list=search&srsearch=${URLEncoder.encode(query, "UTF-8")}" +
                    "&format=json&srlimit=1"

            val searchJson = URL(searchUrl).readText()
            val searchObj = JSONObject(searchJson)
            val searchResults = searchObj
                .getJSONObject("query")
                .getJSONArray("search")

            if (searchResults.length() == 0) return@withContext Pair(null, null)

            val pageTitle = searchResults.getJSONObject(0).getString("title")
            val encodedTitle = URLEncoder.encode(pageTitle, "UTF-8")

            val detailUrl = "https://en.wikipedia.org/w/api.php" +
                    "?action=query&titles=${encodedTitle}" +
                    "&prop=pageimages|coordinates" +
                    "&piprop=original" +
                    "&format=json"

            val detailJson = JSONObject(URL(detailUrl).readText())
            val pages = detailJson
                .getJSONObject("query")
                .getJSONObject("pages")
            val page = pages.getJSONObject(pages.keys().next())

            val imageUrl = page
                .optJSONObject("original")
                ?.optString("source")

            val coordsArray = page.optJSONArray("coordinates")
            val coordinates: Pair<Double, Double>? = if (coordsArray != null && coordsArray.length() > 0) {
                val coord = coordsArray.getJSONObject(0)
                Pair(coord.getDouble("lat"), coord.getDouble("lon"))
            } else null


            val localPath = imageUrl?.let { url ->
                downloadAndSaveImage(url)
            }

            Pair(localPath, coordinates)
        }
        catch (e: Exception) {
            e.printStackTrace()
            Pair(null, null)
        }
    }

    /**
     * Downloads an image from [url] and saves it to the app's internal files directory.
     * Uses the same storage location as camera photos so FileProvider serves them correctly.
     *
     * @return Local file path string, or null if download failed
     */
    private fun downloadAndSaveImage(url: String): String? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return null
            }

            val file = File(
                context.filesDir,
                "itinerary_${System.currentTimeMillis()}.jpg"
            )

            connection.inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            ).toString()
        }
        catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}