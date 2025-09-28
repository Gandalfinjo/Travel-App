package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.PackingDao
import com.example.travelapp.database.models.PackingItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackingRepository @Inject constructor(
    private val packingDao: PackingDao
) {
    fun getItemsForTrip(tripId: Int): Flow<List<PackingItem>> =
        packingDao.getPackingItemsByTripId(tripId)

    suspend fun addItem(item: PackingItem) = packingDao.insert(item)

    suspend fun updateItem(item: PackingItem) = packingDao.update(item)

    suspend fun deleteItem(item: PackingItem) = packingDao.delete(item)
}