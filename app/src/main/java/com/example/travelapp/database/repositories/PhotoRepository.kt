package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.PhotoDao
import com.example.travelapp.database.models.Photo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing trip photos.
 *
 * Handles photo storage, retrieval, and organization for trips.
 */
@Singleton
class PhotoRepository @Inject constructor(
    private val photoDao: PhotoDao
){
    /**
     * Adds a new photo to the database.
     *
     * @param photo Photo to add
     */
    suspend fun addPhoto(photo: Photo) =
        photoDao.insert(photo)

    /**
     * Updates an existing photo's metadata.
     *
     * @param photo Photo to update
     */
    suspend fun updatePhoto(photo: Photo) =
        photoDao.update(photo)

    /**
     * Deletes a photo from the database.
     *
     * @param photo Photo to delete
     */
    suspend fun deletePhoto(photo: Photo) =
        photoDao.delete(photo)

    /**
     * Retrieves all photos for a specific trip, ordered by date taken (newest first).
     *
     * @param tripId ID of the trip
     * @return Flow emitting list of photos
     */
    fun getTripPhotos(tripId: Int): Flow<List<Photo>> =
        photoDao.getByTrip(tripId)
}