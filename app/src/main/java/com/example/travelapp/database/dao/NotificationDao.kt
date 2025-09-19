package com.example.travelapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.travelapp.database.models.AppNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: AppNotification)

    @Update
    suspend fun update(notification: AppNotification)

    @Delete
    suspend fun delete(notification: AppNotification)

    @Query("SELECT * FROM notifications WHERE trip_id = :tripId ORDER BY date ASC")
    fun getByTrip(tripId: Int): Flow<List<AppNotification>>
}