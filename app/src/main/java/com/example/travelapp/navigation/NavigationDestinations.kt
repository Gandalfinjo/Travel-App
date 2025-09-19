package com.example.travelapp.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

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

object TripDetailsDestination: NavigationDestination {
    override val route = "trip_details"
    val arguments = listOf(navArgument("tripId") { type = NavType.IntType} )
    val routeWithArgs = "$route/{tripId}"
}

object WeatherDestination: NavigationDestination {
    override val route = "weather"
    val arguments = listOf(navArgument("location") { type = NavType.StringType } )
    val routeWithArgs = "$route/{location}"
}