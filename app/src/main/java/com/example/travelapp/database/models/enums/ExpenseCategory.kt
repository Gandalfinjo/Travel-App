package com.example.travelapp.database.models.enums

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.travelapp.R

enum class ExpenseCategory(
    @param:StringRes val displayName: Int,
    val icon: ImageVector
) {
    FOOD(R.string.food, Icons.Default.Restaurant),
    ACCOMMODATION(R.string.accommodation, Icons.Default.Hotel),
    TRANSPORT(R.string.transport, Icons.Default.DirectionsCar),
    TICKETS(R.string.tickets, Icons.Default.ConfirmationNumber),
    SOUVENIRS(R.string.souvenirs, Icons.Default.CardGiftcard),
    OTHER(R.string.other, Icons.Default.AttachMoney)
    }