package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.TripDao
import com.example.travelapp.database.models.Trip
import com.example.travelapp.database.models.enums.TripStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing trips.
 *
 * Handles trip creation, retrieval, updates, and deletion.
 * Provides filtering by user and trip status.
 */
@Singleton
class TripRepository @Inject constructor(
    private val tripDao: TripDao
) {
    /**
     * Creates a new trip in the database.
     *
     * @param trip Trip to create (id will be auto-generated)
     * @return Created trip with assigned database ID
     */
    suspend fun createTrip(trip: Trip): Trip {
        val id = tripDao.insert(trip)
        return trip.copy(id = id.toInt())
    }

    /**
     * Updates an existing trip's details.
     *
     * @param trip Trip with updated data
     */
    suspend fun updateTrip(trip: Trip) =
        tripDao.update(trip)

    /**
     * Updates only the status of a trip.
     * Used by background workers for automatic status transitions.
     *
     * @param tripId ID of the trip to update
     * @param tripStatus New status to set
     */
    suspend fun updateTripStatus(tripId: Int, tripStatus: TripStatus) =
        tripDao.updateTripStatus(tripId, tripStatus)

    /**
     * Deletes a trip from the database.
     * Cascades to delete related itinerary items, packing items, photos, etc.
     *
     * @param trip Trip to delete
     */
    suspend fun deleteTrip(trip: Trip) =
        tripDao.delete(trip)

    /**
     * Retrieves a single trip by ID.
     *
     * @param id Trip ID
     * @return Flow emitting the trip or null if not found
     */
    fun getTrip(id: Int): Flow<Trip?> =
        tripDao.getById(id)

    /**
     * Retrieves all trips for a specific user, ordered by start date.
     *
     * @param userId ID of the user
     * @return Flow emitting list of trips
     */
    fun getUserTrips(userId: Int): Flow<List<Trip>> =
        tripDao.getTripsByUser(userId)

    /**
     * Retrieves trips for a user filtered by status, ordered by start date.
     *
     * @param userId ID of the user
     * @param status Trip status to filter by
     * @return Flow emitting list of trips matching the status
     */
    fun getUserTripsByStatus(userId: Int, status: TripStatus): Flow<List<Trip>> =
        tripDao.getTripsByUserAndStatus(userId, status)
}