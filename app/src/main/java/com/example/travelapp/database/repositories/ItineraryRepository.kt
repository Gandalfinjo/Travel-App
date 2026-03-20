package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.ItineraryDao
import com.example.travelapp.database.models.ItineraryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing itinerary items.
 *
 * Provides access to daily activities and plans for trips.
 */
@Singleton
class ItineraryRepository @Inject constructor(
    private val itineraryDao: ItineraryDao
) {
    /**
     * Retrieves all itinerary items for a specific trip, ordered by date.
     *
     * @param tripId ID of the trip
     * @return Flow emitting the list of itinerary items
     */
    fun getItemsForTrip(tripId: Int): Flow<List<ItineraryItem>> =
    itineraryDao.getItineraryItemsByTripId(tripId)

    /**
     * Adds a new itinerary item to the database.
     *
     * @param item Itinerary item to add
     */
    suspend fun addItem(item: ItineraryItem) =
        itineraryDao.insert(item)

    /**
     * Updates an existing itinerary item.
     *
     * @param item Itinerary item to update
     */
    suspend fun updateItem(item: ItineraryItem) =
        itineraryDao.update(item)

    /**
     * Deletes an itinerary item from the database.
     *
     * @param item Itinerary item to delete
     */
    suspend fun deleteItem(item: ItineraryItem) =
        itineraryDao.delete(item)
}