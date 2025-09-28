package com.example.travelapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.travelapp.database.converters.TravelTypeConverters
import com.example.travelapp.database.dao.ItineraryDao
import com.example.travelapp.database.dao.NotificationDao
import com.example.travelapp.database.dao.PhotoDao
import com.example.travelapp.database.dao.PlaceDao
import com.example.travelapp.database.dao.TripDao
import com.example.travelapp.database.dao.UserDao
import com.example.travelapp.database.models.AppNotification
import com.example.travelapp.database.models.ItineraryItem
import com.example.travelapp.database.models.Photo
import com.example.travelapp.database.models.Place
import com.example.travelapp.database.models.Trip
import com.example.travelapp.database.models.User

@TypeConverters(TravelTypeConverters::class)
@Database(
    entities = [
        User::class,
        Trip::class,
        Place::class,
        Photo::class,
        AppNotification::class,
        ItineraryItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TravelDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun placeDao(): PlaceDao
    abstract fun photoDao(): PhotoDao
    abstract fun notificationDao(): NotificationDao
    abstract fun itineraryDao(): ItineraryDao

    companion object {
        private var INSTANCE: TravelDatabase? = null

        fun getDatabase(context: Context): TravelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelDatabase::class.java,
                    "travel_database"
                ).build()

                INSTANCE = instance

                return@synchronized instance
            }
        }
    }
}