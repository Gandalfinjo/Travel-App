package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.PhotoDao
import com.example.travelapp.database.models.Photo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    private val photoDao: PhotoDao
){
    suspend fun addPhoto(photo: Photo) =
        photoDao.insert(photo)

    suspend fun updatePhoto(photo: Photo) =
        photoDao.update(photo)

    suspend fun deletePhoto(photo: Photo) =
        photoDao.delete(photo)

    fun getTripPhotos(tripId: Int): Flow<List<Photo>> =
        photoDao.getByTrip(tripId)
}