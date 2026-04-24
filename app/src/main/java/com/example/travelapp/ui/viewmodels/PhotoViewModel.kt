package com.example.travelapp.ui.viewmodels

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Geocoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.models.Photo
import com.example.travelapp.database.repositories.PhotoRepository
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

/**
 * ViewModel responsible for managing trip photos.
 *
 * Handles adding and retrieving photos associated with a specific trip.
 * Acts as a bridge between UI and [PhotoRepository].
 */
@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private val _savedPhotoIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedPhotoIds: StateFlow<Set<Int>> = _savedPhotoIds.asStateFlow()

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _resolvableException = MutableStateFlow<ResolvableApiException?>(null)
    val resolvableException: StateFlow<ResolvableApiException?> = _resolvableException.asStateFlow()

    private val _launchCamera = MutableStateFlow(false)
    val launchCamera: StateFlow<Boolean> = _launchCamera.asStateFlow()


    private val locationCache = mutableMapOf<String, GeoPoint>()
    private val _tripGeoPoint = MutableStateFlow<GeoPoint?>(null)
    val tripGeoPoint: StateFlow<GeoPoint?> = _tripGeoPoint.asStateFlow()

    private val _isGeocoding = MutableStateFlow(false)
    val isGeocoding: StateFlow<Boolean> = _isGeocoding.asStateFlow()

    /**
     * Adds a new photo for a trip.
     *
     * Persists the photo using [PhotoRepository].
     *
     * @param photo [Photo] object containing image data and metadata
     */
    fun addPhoto(photo: Photo) = viewModelScope.launch {
        photoRepository.addPhoto(photo)
    }

    /**
     * Retrieves all photos for a specific trip as a Flow.
     *
     * Allows observing changes to the photo list in real-time.
     *
     * @param tripId ID of the trip whose photos should be retrieved
     * @return Flow emitting a list of [Photo] objects
     */
    fun getTripPhotos(tripId: Int): Flow<List<Photo>> =
        photoRepository.getTripPhotos(tripId)

    /**
     * Updates the specified photo.
     *
     * @param photo Photo to update
     */
    fun updatePhoto(photo: Photo) = viewModelScope.launch {
        photoRepository.updatePhoto(photo)
    }

    /**
     * Deletes the specified photo from the app.
     *
     * @param photo Photo to delete
     */
    fun deletePhoto(photo: Photo) = viewModelScope.launch {
        photoRepository.deletePhoto(photo)
    }

    fun clearResolvableException() {
        _resolvableException.update { null }
    }

    @SuppressLint("MissingPermission")
    fun checkLocationSettingsAndPrepareCamera() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        LocationServices.getSettingsClient(context)
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                _launchCamera.update { true }
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    _resolvableException.update { exception }
                }
                else {
                    _launchCamera.update { true }
                }
            }
    }

    fun onCameraLaunched() {
        _launchCamera.update { false }
    }

    fun savePhotoToGallery(
        contentResolver: ContentResolver,
        photo: Photo
    ) = viewModelScope.launch(Dispatchers.IO) {
        val bitmap = ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(contentResolver, photo.filePath.toUri())
        )

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "travel_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TravelApp")
        }

        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        uri?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }

            withContext(Dispatchers.Main) {
                _savedPhotoIds.update { it + photo.id }
            }
        }
    }

    fun resolveTripLocation(locationName: String?) {
        if (locationName.isNullOrBlank()) {
            _tripGeoPoint.value = null
            return
        }

        locationCache[locationName]?.let {
            _tripGeoPoint.value = it
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isGeocoding.value = true

            try {
                val geocoder = Geocoder(context)
                val results = geocoder.getFromLocationName(locationName, 1)

                val geoPoint = if (!results.isNullOrEmpty()) {
                    GeoPoint(results[0].latitude, results[0].longitude)
                } else null

                geoPoint?.let {
                    locationCache[locationName] = it
                }

                withContext(Dispatchers.Main) {
                    _tripGeoPoint.value = geoPoint
                    _isGeocoding.value = false
                }
            }
            catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _tripGeoPoint.value = null
                    _isGeocoding.value = false
                }
            }
        }
    }
}