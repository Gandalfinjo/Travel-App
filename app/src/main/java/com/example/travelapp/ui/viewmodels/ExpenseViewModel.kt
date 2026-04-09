package com.example.travelapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.travelapp.api.repositories.CurrencyRepository
import com.example.travelapp.database.models.CategoryTotal
import com.example.travelapp.database.models.Expense
import com.example.travelapp.database.models.enums.ExpenseCategory
import com.example.travelapp.database.repositories.ExpenseRepository
import com.example.travelapp.database.repositories.TripRepository
import com.example.travelapp.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val totalSpent: Double = 0.0,
    val totalByCategory: List<CategoryTotal> = emptyList(),
    val selectedCategory: ExpenseCategory? = null, // null means all categories
    val errorMessage: String? = null
)

/**
 * ViewModel responsible for managing expense-related UI state and business logic.
 *
 * Handles expense creation, loading and deletion
 * Coordinates with [ExpenseRepository] and [TripRepository] for data persistence.
 *
 * Uses [CurrencyRepository] for conversion rates and [SessionManager] for working with session.
 */
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val tripRepository: TripRepository,
    private val currencyRepository: CurrencyRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    /**
     * Loads all expenses for the specified trip.
     *
     * Updates the [uiState] with the list of expenses, total amount spent and total amount spent by category.
     *
     * @param tripId ID of the trip for which to load expenses
     */
    fun loadExpenses(tripId: Int) = viewModelScope.launch {
        launch {
            expenseRepository.getExpensesByTripId(tripId).collect { expenses ->
                _uiState.update {
                    it.copy(
                        expenses = expenses,
                    )
                }
            }
        }

        launch {
            expenseRepository.getTotalByTrip(tripId).collect { total ->
                _uiState.update {
                    it.copy(totalSpent = total ?: 0.0)
                }
            }
        }

        launch {
            expenseRepository.getTotalByCategory(tripId).collect { totals ->
                _uiState.update {
                    it.copy(totalByCategory = totals)
                }
            }
        }
    }

    /**
     * Adds a new expense for the specified trip.
     *
     * @param tripId ID of the trip for which to add the expense
     * @param amount Expense amount
     * @param currency Expense currency
     * @param category Expense Category
     * @param description Optional description of the expense
     * @param date Expense date (null if category is accommodation)
     */
    fun addExpense(
        tripId: Int,
        amount: Double,
        currency: String,
        category: ExpenseCategory,
        description: String?,
        date: LocalDate?
    ) = viewModelScope.launch {
        val trip = tripRepository.getTrip(tripId).first() ?: return@launch
        val appDefaultCurrency = sessionManager.defaultCurrency.first()

        val amountInTripCurrency = currencyRepository.convert(
            amount = amount,
            from = currency,
            to = trip.currency
        )

        val amountInDefaultCurrency = currencyRepository.convert(
            amount = amount,
            from = currency,
            to = appDefaultCurrency
        )

        val expense = Expense(
            tripId = tripId,
            amount = amount,
            currency = currency,
            amountInTripCurrency = amountInTripCurrency,
            amountInDefaultCurrency = amountInDefaultCurrency,
            category = category,
            description = description.takeIf { !it.isNullOrBlank() },
            date = date
        )

        expenseRepository.addExpense(expense)
    }

    /**
     * Selects a category for which to filter expenses for the specified trip.
     *
     * Updates the [uiState] with the list of filtered expenses.
     *
     * @param tripId ID of the trip for which to show expenses
     * @param category Expense category for which to filter the expenses
     */
    fun selectCategory(tripId: Int, category: ExpenseCategory?) = viewModelScope.launch {
        _uiState.update { it.copy(selectedCategory = category) }

        if (category == null) {
            expenseRepository.getExpensesByTripId(tripId).collect { expenses ->
                _uiState.update { it.copy(expenses = expenses) }
            }
        }
        else {
            expenseRepository.getExpensesByTripIdAndCategory(tripId, category).collect { expenses ->
                _uiState.update { it.copy(expenses = expenses) }
            }
        }
    }

    /**
     * Deletes an expense.
     *
     * Removes the expense from persistence using [ExpenseRepository].
     *
     * @param expense [Expense] to be deleted
     */
    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.deleteExpense(expense)
    }

    suspend fun convertToTripCurrency(amount: Double, currency: String, tripCurrency: String) =
        currencyRepository.convert(amount, currency, tripCurrency)
}