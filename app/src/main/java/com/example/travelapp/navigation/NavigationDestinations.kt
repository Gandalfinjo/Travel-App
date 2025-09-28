package com.example.travelapp.navigation

import android.content.Intent
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

interface NavigationDestination {
    val route: String
}

object LoginDestination : NavigationDestination {
    override val route = "login"
}

object RegisterDestination : NavigationDestination {
    override val route = "register"
}

object DashboardDestination : NavigationDestination {
    override val route = "dashboard"
}

object AddTripDestination : NavigationDestination {
    override val route = "add_trip"
}

object TripListDestination : NavigationDestination {
    override val route = "trip_list"
}

object TripDetailsDestination : NavigationDestination {
    override val route = "trip_details"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType} )
    val routeWithArgs = "$route/{tripId}"
    val deepLink = navDeepLink {
        uriPattern = "travelapp://trip_details/{tripId}"
        action = Intent.ACTION_VIEW
    }
}

object WeatherDestination : NavigationDestination {
    override val route = "weather"
    val arguments = listOf(navArgument("location") { type = NavType.StringType } )
    val routeWithArgs = "$route/{location}"
}

object MapDestination : NavigationDestination {
    override val route = "map"
}

object AlbumDestination : NavigationDestination {
    override val route = "album"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType } )
    val routeWithArgs = "$route/{tripId}"
}

object ItineraryDestination : NavigationDestination {
    override val route = "itinerary_items"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType } )
    val routeWithArgs = "$route/{tripId}"
}

object AddItineraryDestination : NavigationDestination {
    override val route = "add_itinerary"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType } )
    val routeWithArgs = "$route/{tripId}"
}

object PackingDestination : NavigationDestination {
    override val route = "packing_items"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType } )
    val routeWithArgs = "$route/{tripId}"
}

object AddPackingItemDestination : NavigationDestination {
    override val route = "add_packing_item"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType } )
    val routeWithArgs = "$route/{tripId}"
}