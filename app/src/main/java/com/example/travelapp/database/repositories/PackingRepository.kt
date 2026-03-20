package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.PackingDao
import com.example.travelapp.database.models.PackingItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing packing list items.
 *
 * Handles items that users neet to pack for their trips.
 */
@Singleton
class PackingRepository @Inject constructor(
    private val packingDao: PackingDao
) {
    /**
     * Retrieves all packing items for a specific trip.
     *
     * @param tripId ID of the trip
     * @return Flow emitting the list of packing items
     */
    fun getItemsForTrip(tripId: Int): Flow<List<PackingItem>> =
        packingDao.getPackingItemsByTripId(tripId)

    /**
     * Adds a new packing item to the database.
     *
     * @param item Packing item to add
     */
    suspend fun addItem(item: PackingItem) = packingDao.insert(item)

    /**
     * Updates an existing packing item (e.g., marking as packed)
     *
     * @param item Packing item to update
     */
    suspend fun updateItem(item: PackingItem) = packingDao.update(item)

    /**
     * Deletes a packing item from the database.
     *
     * @param item Packing item to delete
     */
    suspend fun deleteItem(item: PackingItem) = packingDao.delete(item)
}