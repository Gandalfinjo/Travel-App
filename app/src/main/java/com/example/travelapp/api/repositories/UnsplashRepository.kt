package com.example.travelapp.api.repositories

import android.content.Context
import android.net.Uri
import com.example.travelapp.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import javax.inject.Inject

class UnsplashRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val client = OkHttpClient()
    private val accessKey = BuildConfig.UNSPLASH_API_KEY

    /**
     * Searches Unsplash for a photo matching the query, downloads it,
     * saves it to internal storage and returns the local file path.
     *
     * @param query Search term (e.g. itinerary item title)
     * @return Local file path string, or null if anything failed
     */
    suspend fun fetchAndSavePhoto(query: String): String? = withContext(Dispatchers.IO) {
        try {
            val searchUrl = "https://api.unsplash.com/search/photos" +
                    "?query=${Uri.encode(query)}&per_page=1&orientation=landscape"

            val searchRequest = Request.Builder()
                .url(searchUrl)
                .header("Authorization", "Client-ID $accessKey")
                .build()

            val searchResponse = client.newCall(searchRequest).execute()
            if (!searchResponse.isSuccessful) return@withContext null

            val json = JSONObject(searchResponse.body?.string() ?: return@withContext null)
            val results = json.getJSONArray("results")

            if (results.length() == 0) return@withContext null

            val imageUrl = results
                .getJSONObject(0)
                .getJSONObject("urls")
                .getString("regular")

            val imageRequest = Request.Builder().url(imageUrl).build()
            val imageResponse = client.newCall(imageRequest).execute()

            if (!imageResponse.isSuccessful) return@withContext null

            val fileName = "itinerary_${query.take(20).replace(" ", "_")}_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)

            file.outputStream().use { output ->
                imageResponse.body?.byteStream()?.copyTo(output)
            }


            file.absolutePath
        }
        catch (_: Exception) {
            null
        }
    }
}