package com.example.travelapp.navigation

import android.content.Intent
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

/**
 * Interface defining navigation destinations in the app.
 */
interface NavigationDestination {
    val route: String
}

/**
 * Login screen destination.
 */
object LoginDestination : NavigationDestination {
    override val route = "login"
}

/**
 * Registration screen destination.
 */
object RegisterDestination : NavigationDestination {
    override val route = "register"
}

/**
 * Dashboard (home) screen destination.
 */
object DashboardDestination : NavigationDestination {
    override val route = "dashboard"
}

/**
 * Add new trip screen destination.
 */
object AddTripDestination : NavigationDestination {
    override val route = "add_trip"
}

/**
 * Trip list screen destination.
 */
object TripListDestination : NavigationDestination {
    override val route = "trip_list"
}

/**
 * Trip details screen destination.
 * Requires tripId parameter and supports deep linking.
 */
object TripDetailsDestination : NavigationDestination {
    override val route = "trip_details"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType} )
    val routeWithArgs = "$route/{tripId}"
    val deepLink = navDeepLink {
        uriPattern = "travelapp://trip_details/{tripId}"
        action = Intent.ACTION_VIEW
    }
}

/**
 * Weather screen destination.
 * Requires location parameter.
 */
object WeatherDestination : NavigationDestination {
    override val route = "weather"
    val arguments = listOf(navArgument("location") { type = NavType.StringType } )
    val routeWithArgs = "$route/{location}"
}

/**
 * Map screen destination showing nearby points of interest.
 */
object MapDestination : NavigationDestination {
    override val route = "map"
}

/**
 * Photo album screen destination.
 * Requires tripId parameter.
 */
object AlbumDestination : NavigationDestination {
    override val route = "album"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType } )
    val routeWithArgs = "$route/{tripId}"
}

/**
 * Daily itinerary screen destination.
 * Requires tripId parameter.
 */
object ItineraryDestination : NavigationDestination {
    override val route = "itinerary_items"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType } )
    val routeWithArgs = "$route/{tripId}"
}

/**
 * Add itinerary item screen destination.
 * Requires tripId parameter.
 */
object AddItineraryDestination : NavigationDestination {
    override val route = "add_itinerary"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType } )
    val routeWithArgs = "$route/{tripId}"
}

/**
 * Packing list screen destination.
 * Requires tripId parameter.
 */
object PackingDestination : NavigationDestination {
    override val route = "packing_items"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType } )
    val routeWithArgs = "$route/{tripId}"
}

/**
 * Add packing item screen destination.
 * Requires tripId parameter.
 */
object AddPackingItemDestination : NavigationDestination {
    override val route = "add_packing_item"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType } )
    val routeWithArgs = "$route/{tripId}"
}

/**
 * Statistics screen destination showing trip analytics.
 */
object StatisticsDestination : NavigationDestination {
    override val route = "statistics"
}

/**
 * AI trip suggestions screen destination.
 */
object AiSuggestionsDestination : NavigationDestination {
    override val route = "ai_suggestions"
}

/**
 * Expense screen destination.
 */
object ExpenseDestination : NavigationDestination {
    override val route = "expenses"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType })
    val routeWithArgs = "$route/{tripId}"
}

/**
 * Add Expense screen destination.
 */
object AddExpenseDestination : NavigationDestination {
    override val route = "add_expense"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType })
    val routeWithArgs = "$route/{tripId}"
}