package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.TripDao
import com.example.travelapp.database.models.Trip
import com.example.travelapp.database.models.enums.TripStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val tripDao: TripDao
) {
    suspend fun createTrip(trip: Trip) =
        tripDao.insert(trip)

    suspend fun updateTrip(trip: Trip) =
        tripDao.update(trip)

    suspend fun deleteTrip(trip: Trip) =
        tripDao.delete(trip)

    fun getTrip(id: Int): Flow<Trip?> =
        tripDao.getById(id)

    fun getUserTrips(userId: Int): Flow<List<Trip>> =
        tripDao.getTripsByUser(userId)

    fun getUserTripsByStatus(userId: Int, status: TripStatus): Flow<List<Trip>> =
        tripDao.getTripsByUserAndStatus(userId, status)
}