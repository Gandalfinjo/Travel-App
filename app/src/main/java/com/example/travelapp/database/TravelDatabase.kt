package com.example.travelapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.travelapp.database.converters.TravelTypeConverters
import com.example.travelapp.database.dao.ExpenseDao
import com.example.travelapp.database.dao.ItineraryDao
import com.example.travelapp.database.dao.NotificationDao
import com.example.travelapp.database.dao.PackingDao
import com.example.travelapp.database.dao.PhotoDao
import com.example.travelapp.database.dao.PlaceDao
import com.example.travelapp.database.dao.TripDao
import com.example.travelapp.database.dao.UserDao
import com.example.travelapp.database.models.AppNotification
import com.example.travelapp.database.models.Expense
import com.example.travelapp.database.models.ItineraryItem
import com.example.travelapp.database.models.PackingItem
import com.example.travelapp.database.models.Photo
import com.example.travelapp.database.models.Place
import com.example.travelapp.database.models.Trip
import com.example.travelapp.database.models.User

/**
 * Room database for the Travel App.
 *
 * Manages all persistent data including users, trips, itineraries, packing lists,
 * photos, places, and notifications.
 *
 * Current schema version: 4
 * - Version 1: Initial schema (users, trips, places, photos, notifications)
 * - Version 2: Added itinerary_items and packing_items tables
 * - Version 3: Added expanses table
 * - Version 4: Made date in the expanses table nullable
 */
@TypeConverters(TravelTypeConverters::class)
@Database(
    entities = [
        User::class,
        Trip::class,
        Place::class,
        Photo::class,
        AppNotification::class,
        ItineraryItem::class,
        PackingItem::class,
        Expense::class
    ],
    version = 5,
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
    abstract fun expenseDao(): ExpenseDao

    companion object {
        private var INSTANCE: TravelDatabase? = null

        /**
         * Returns singleton instance of the database.
         * Creates a new instance if one doesn't exist.
         *
         * @param context Application context
         * @return TravelDatabase instance
         */
        fun getDatabase(context: Context): TravelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TravelDatabase::class.java,
                    "travel_database"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build()

                INSTANCE = instance

                return@synchronized instance
            }
        }

        /**
         * Migration from database version 1 to 2.
         * Adds itinerary_items and packing_items tables.
         */
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

        /**
         * Migration from database version 2 to 3.
         * Adds expenses table.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS expenses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        trip_id INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        currency TEXT NOT NULL,
                        category TEXT NOT NULL,
                        description TEXT,
                        date INTEGER NOT NULL,
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY(trip_id) REFERENCES trips(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_expenses_trip_id ON expenses(trip_id)"
                )
            }
        }

        /**
         * Migration from database version 3 to 4.
         * Makes date in the expanses table nullable.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE expenses_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        trip_id INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        currency TEXT NOT NULL,
                        category TEXT NOT NULL,
                        description TEXT,
                        date INTEGER,
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY(trip_id) REFERENCES trips(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL("INSERT INTO expenses_new SELECT * FROM expenses")
                db.execSQL("DROP TABLE expenses")
                db.execSQL("ALTER TABLE expenses_new RENAME TO expenses")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_trip_id ON expenses(trip_id)")
            }
        }

        /**
         * Migration from database version 4 to 5.
         * Adds the profile_picture_path column to the users table
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN profile_picture_path TEXT")
            }
        }
    }
}