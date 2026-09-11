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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
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

    private val selectedCategoryState = MutableStateFlow<ExpenseCategory?>(null)
    private var loadJob: Job? = null

    /**
     * Loads all expenses for the specified trip.
     *
     * Updates the [uiState] with the list of expenses, total amount spent and total amount spent by category.
     *
     * @param tripId ID of the trip for which to load expenses
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadExpenses(tripId: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            combine(
                selectedCategoryState.flatMapLatest { category ->
                    if (category == null) {
                        expenseRepository.getExpensesByTripId(tripId)
                    }
                    else {
                        expenseRepository.getExpensesByTripIdAndCategory(tripId, category)
                    }
                },
                expenseRepository.getTotalByTrip(tripId),
                expenseRepository.getTotalByCategory(tripId)
            ) { expenses, total, totalsByCategory ->
                ExpenseUiState(
                    expenses = expenses,
                    totalSpent = total ?: 0.0,
                    totalByCategory = totalsByCategory,
                    selectedCategory = selectedCategoryState.value,
                    errorMessage = null
                )
            }.collect { newState ->
                _uiState.value = newState
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
        try {
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
                description = description?.takeIf { it.isNotBlank() },
                date = date
            )

            expenseRepository.addExpense(expense)
        }
        catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Failed to save expense") }
        }
    }

    /**
     * Selects a category for which to filter expenses for the specified trip.
     *
     * Updates the [uiState] with the list of filtered expenses.
     *
     * @param category Expense category for which to filter the expenses
     */
    fun selectCategory(category: ExpenseCategory?) = viewModelScope.launch {
        selectedCategoryState.value = category
        _uiState.update { it.copy(selectedCategory = category) }
    }

    /**
     * Deletes an expense.
     *
     * Removes the expense from persistence using [ExpenseRepository].
     *
     * @param expense [Expense] to be deleted
     */
    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        try {
            expenseRepository.deleteExpense(expense)
        }
        catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = e.localizedMessage ?: "Failed to delete expense") }
        }
    }

    suspend fun convertToTripCurrency(amount: Double, currency: String, tripCurrency: String) =
        currencyRepository.convert(amount, currency, tripCurrency)
}