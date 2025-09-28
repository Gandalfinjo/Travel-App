package com.example.travelapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.travelapp.database.converters.TravelTypeConverters
import com.example.travelapp.database.dao.ItineraryDao
import com.example.travelapp.database.dao.NotificationDao
import com.example.travelapp.database.dao.PackingDao
import com.example.travelapp.database.dao.PhotoDao
import com.example.travelapp.database.dao.PlaceDao
import com.example.travelapp.database.dao.TripDao
import com.example.travelapp.database.dao.UserDao
import com.example.travelapp.database.models.AppNotification
import com.example.travelapp.database.models.ItineraryItem
import com.example.travelapp.database.models.PackingItem
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
        ItineraryItem::class,
        PackingItem::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TravelDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun placeDao(): PlaceDao
    abstract fun photoDao(): PhotoDao
    abstract fun notificationDao(): NotificationDao
    abstract fun itineraryDao(): ItineraryDao
    abstract fun packingDao(): PackingDao

    companion object {
        private var INSTANCE: TravelDatabase? = null

        fun getDatabase(context: Context): TravelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelDatabase::class.java,
                    "travel_database"
                ).addMigrations(MIGRATION_1_2).build()

                INSTANCE = instance

                return@synchronized instance
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS itinerary_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tripId INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT,
                        isDone INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(tripId) REFERENCES trips(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS packing_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        tripId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        quantity INTEGER NOT NULL DEFAULT 1,
                        isPacked INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(tripId) REFERENCES trips(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
            }
        }
    }
}