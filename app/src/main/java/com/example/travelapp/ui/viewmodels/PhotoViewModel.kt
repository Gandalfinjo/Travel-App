package com.example.travelapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.database.models.Photo
import com.example.travelapp.database.repositories.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing trip photos.
 *
 * Handles adding and retrieving photos associated with a specific trip.
 * Acts as a bridge between UI and [PhotoRepository].
 */
@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {
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
}