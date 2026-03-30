package com.example.travelapp.database.models

import com.example.travelapp.database.models.enums.ExpenseCategory

data class CategoryTotal(
    val category: ExpenseCategory,
    val total: Double
)
