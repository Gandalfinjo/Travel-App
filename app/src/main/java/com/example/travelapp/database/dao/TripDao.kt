package com.example.travelapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.travelapp.database.models.Trip
import com.example.travelapp.database.models.enums.TripStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trip: Trip): Long

    @Update
    suspend fun update(trip: Trip)

    @Delete
    suspend fun delete(trip: Trip)

    @Query("SELECT * FROM trips WHERE id = :id")
    fun getById(id: Int): Flow<Trip?>

    @Query("SELECT * FROM trips WHERE user_id = :userId ORDER BY start_date ASC")
    fun getTripsByUser(userId: Int): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE user_id = :userId AND status = :status ORDER BY start_date ASC")
    fun getTripsByUserAndStatus(userId: Int, status: TripStatus): Flow<List<Trip>>
}