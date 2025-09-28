package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.ItineraryDao
import com.example.travelapp.database.models.ItineraryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItineraryRepository @Inject constructor(
    private val itineraryDao: ItineraryDao
) {
    fun getItemsForTrip(tripId: Int): Flow<List<ItineraryItem>> =
    itineraryDao.getItineraryItemsByTripId(tripId)

    suspend fun addItem(item: ItineraryItem) =
        itineraryDao.insert(item)

    suspend fun updateItem(item: ItineraryItem) =
        itineraryDao.update(item)

    suspend fun deleteItem(item: ItineraryItem) =
        itineraryDao.delete(item)
}