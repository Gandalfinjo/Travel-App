package com.example.travelapp.api.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Sealed interface representing the outcome of a device location request operation.
 */
sealed interface LocationResult {
    data class Success(val geoPoint: GeoPoint) : LocationResult
    object Failure : LocationResult
    data class ResolutionRequired(val exception: ResolvableApiException) : LocationResult
}

/**
 * Repository responsible for interfacing with Android system location services and hardware.
 *
 * Encapsulates interactions with Google Play Services (Fused Location Provider, Settings Client)
 * and the system [Geocoder], converting callback-driven APIs into sequential, suspending coroutines.
 *
 * @property context The application context required to initialize system location clients.
 */
@Singleton
class LocationRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val settingsClient = LocationServices.getSettingsClient(context)

    /**
     * Assesses the device's system location settings and attempts to fetch the current GPS coordinates.
     *
     * This function safely bridges Google Play Services Task callbacks into Kotlin coroutines using
     * a cancellable continuation framework. It performs all operations off the main thread.
     *
     * Requires runtime location permissions (`ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION`)
     * to be granted prior to execution.
     *
     * @return A [LocationResult] detailing whether the operation succeeded, failed, or requires
     * a settings dialogue resolution from the user.
     * @throws SecurityException If called without appropriate location permissions.
     */
    @SuppressLint("MissingPermission")
    suspend fun checkSettingsAndGetLocation(): LocationResult = withContext(Dispatchers.IO) {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
            val settingsRequest = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()

            suspendCancellableCoroutine { continuation ->
                settingsClient.checkLocationSettings(settingsRequest)
                    .addOnSuccessListener { continuation.resume(Unit) }
                    .addOnFailureListener { exception -> continuation.resumeWithException(exception) }
            }

            val location = suspendCancellableCoroutine { continuation ->
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { loc -> continuation.resume(loc) }
                    .addOnFailureListener { ex -> continuation.resumeWithException(ex) }
            }

            if (location != null) {
                LocationResult.Success(GeoPoint(location.latitude, location.longitude))
            }
            else {
                LocationResult.Failure
            }
        }
        catch (e: Exception) {
            if (e is ResolvableApiException) {
                LocationResult.ResolutionRequired(e)
            }
            else {
                LocationResult.Failure
            }
        }
    }

    /**
     * Resolves a human-readable address or place description string into geographic coordinates.
     *
     * Utilizes forward geocoding backed by the Android system [Geocoder] service. Because geocoding
     * relies on network lookups, this execution is explicitly bound to [Dispatchers.IO].
     *
     * @param address The textual description of the location (e.g., "Paris", "1600 Amphitheatre Pkwy").
     * @return A [GeoPoint] representing the top matching result's coordinates, or null if no
     * matches are found or a network error occurs.
     */
    suspend fun getCoordinatesFromAddress(address: String): GeoPoint? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context)
            val results = geocoder.getFromLocationName(address, 1)

            if (!results.isNullOrEmpty()) {
                GeoPoint(results[0].latitude, results[0].longitude)
            }
            else {
                null
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}