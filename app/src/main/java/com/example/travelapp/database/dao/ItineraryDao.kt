package com.example.travelapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.travelapp.database.models.ItineraryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ItineraryDao {
    @Query("SELECT * FROM itinerary_items WHERE tripId = :tripId ORDER BY date")
    fun getItineraryItemsByTripId(tripId: Int): Flow<List<ItineraryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItineraryItem)

    @Update
    suspend fun update(item: ItineraryItem)

    @Delete
    suspend fun delete(item: ItineraryItem)
}