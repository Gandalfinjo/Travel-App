package com.example.travelapp.database.repositories

import com.example.travelapp.database.dao.ExpenseDao
import com.example.travelapp.database.models.CategoryTotal
import com.example.travelapp.database.models.Expense
import com.example.travelapp.database.models.enums.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing expenses.
 *
 * Handles expense creation, deletion, retrieving and filtering.
 */
@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    /**
     * Adds a new expense to the database.
     *
     * @param expense Expense to add (id will be auto-generated)
     * @return ID of the added expense
     */
    suspend fun addExpense(expense: Expense): Long =
        expenseDao.insert(expense)

    /**
     * Deletes an expense from the database.
     *
     * @param expense Expense to delete from the database
     */
    suspend fun deleteExpense(expense: Expense) =
        expenseDao.delete(expense)

    /**
     * Gets all expenses for the specified trip ordered by date.
     *
     * @param tripId ID of the trip for which to retrieve the expenses
     * @return Flow emitting list of expenses
     */
    fun getExpensesByTripId(tripId: Int): Flow<List<Expense>> =
        expenseDao.getExpensesByTripId(tripId)

    /**
     * Gets all expenses for the specified trip, filtered by the specified category
     *
     * @param tripId ID of the trip for which to retrieve the expenses
     * @param category [ExpenseCategory] to filter the expenses with
     * @return Flow emitting list of expenses
     */
    fun getExpensesByTripIdAndCategory(tripId: Int, category: ExpenseCategory): Flow<List<Expense>> =
        expenseDao.getExpensesByTripIdAndCategory(tripId, category)

    /**
     * Gets the total amount spent for a specific trip.
     *
     * @param tripId ID of the trip for which to retrieve the amount spent
     * @return Flow emitting total amount spent
     */
    fun getTotalByTrip(tripId: Int): Flow<Double?> =
        expenseDao.getTotalByTrip(tripId)

    /**
     * Ges the total amount spent for a specific trip, grouped by category.
     *
     * @param tripId ID of the trip for which to retrieve the amount spent.
     * @return Flow emitting list of [CategoryTotal]
     */
    fun getTotalByCategory(tripId: Int): Flow<List<CategoryTotal>> =
        expenseDao.getTotalByCategory(tripId)
}