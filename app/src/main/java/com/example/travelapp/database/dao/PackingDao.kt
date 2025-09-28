package com.example.travelapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.travelapp.database.models.PackingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PackingDao {
    @Query("SELECT * FROM packing_items WHERE tripId = :tripId")
    fun getPackingItemsByTripId(tripId: Int): Flow<List<PackingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PackingItem)

    @Update
    suspend fun update(item: PackingItem)

    @Delete
    suspend fun delete(item: PackingItem)
}