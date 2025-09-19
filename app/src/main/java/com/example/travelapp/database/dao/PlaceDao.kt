package com.example.travelapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.travelapp.database.models.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: Place)

    @Update
    suspend fun update(place: Place)

    @Delete
    suspend fun delete(place: Place)

    @Query("SELECT * FROM places WHERE trip_id = :tripId ORDER BY name ASC")
    fun getPlacesByTrip(tripId: Int): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getById(id: Int): Place?
}