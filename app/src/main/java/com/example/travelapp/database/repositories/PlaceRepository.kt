package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.PlaceDao
import com.example.travelapp.database.models.Place
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepository @Inject constructor(
    private val placeDao: PlaceDao
) {
    suspend fun addPlace(place: Place) =
        placeDao.insert(place)

    suspend fun updatePlace(place: Place) =
        placeDao.update(place)

    suspend fun deletePlace(place: Place) =
        placeDao.delete(place)

    fun getTripPlaces(tripId: Int): Flow<List<Place>> =
        placeDao.getPlacesByTrip(tripId)

    suspend fun getPlace(id: Int): Place? =
        placeDao.getById(id)
}