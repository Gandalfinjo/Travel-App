package com.example.travelapp.database.models.enums

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.travelapp.R

enum class ExpenseCategory {
    FOOD,
    ACCOMMODATION,
    TRANSPORT,
    TICKETS,
    SOUVENIRS,
    OTHER
}

/**
 * Returns the name of the expense category as an id of the string resource
 */
fun ExpenseCategory.displayName(): Int = when (this) {
    ExpenseCategory.FOOD -> R.string.food
    ExpenseCategory.ACCOMMODATION -> R.string.accommodation
    ExpenseCategory.TRANSPORT -> R.string.accommodation
    ExpenseCategory.TICKETS -> R.string.tickets
    ExpenseCategory.SOUVENIRS -> R.string.souvenirs
    ExpenseCategory.OTHER -> R.string.other
}

/**
 * Returns the icon to show for the expense category
 */
fun ExpenseCategory.icon(): ImageVector = when (this) {
    ExpenseCategory.FOOD -> Icons.Default.Restaurant
    ExpenseCategory.ACCOMMODATION -> Icons.Default.Hotel
    ExpenseCategory.TRANSPORT -> Icons.Default.DirectionsCar
    ExpenseCategory.TICKETS -> Icons.Default.ConfirmationNumber
    ExpenseCategory.SOUVENIRS -> Icons.Default.CardGiftcard
    ExpenseCategory.OTHER -> Icons.Default.AttachMoney
}