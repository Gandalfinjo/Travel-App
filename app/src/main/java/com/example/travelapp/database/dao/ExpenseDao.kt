package com.example.travelapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.travelapp.database.models.CategoryTotal
import com.example.travelapp.database.models.Expense
import com.example.travelapp.database.models.enums.ExpenseCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE trip_id = :tripId ORDER BY date DESC")
    fun getExpensesByTripId(tripId: Int): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE trip_id = :tripId AND category = :category ORDER BY date DESC")
    fun getExpensesByTripIdAndCategory(tripId: Int, category: ExpenseCategory): Flow<List<Expense>>

    @Query("SELECT SUM(amount_in_trip_currency) FROM expenses WHERE trip_id = :tripId")
    fun getTotalByTrip(tripId: Int): Flow<Double?>

    @Query("SELECT category, SUM(amount_in_trip_currency) as total FROM expenses where trip_id = :tripId GROUP BY category")
    fun getTotalByCategory(tripId: Int): Flow<List<CategoryTotal>>

    @Query("""
    SELECT e.category, SUM(e.amount_in_default_currency) as total
    FROM expenses e 
    INNER JOIN trips t ON e.trip_id = t.id 
    WHERE t.user_id = :userId 
    GROUP BY e.category
    ORDER BY total DESC
    """)
    fun getTotalByCategoryForUser(userId: Int): Flow<List<CategoryTotal>>

    @Query("SELECT SUM(amount) FROM expenses WHERE trip_id = :tripId")
    suspend fun getTotalSpentForTrip(tripId: Int): Double?

    @Query("SELECT SUM(amount_in_default_currency) FROM expenses where trip_id = :tripId")
    suspend fun getTotalSpentInDefaultCurrencyForTrip(tripId: Int): Double?

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesOnce(): List<Expense>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<Expense>)
}