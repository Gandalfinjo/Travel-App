package com.example.travelapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.travelapp.navigation.AddItineraryDestination
import com.example.travelapp.navigation.AddPackingItemDestination
import com.example.travelapp.navigation.AddTripDestination
import com.example.travelapp.navigation.AiSuggestionsDestination
import com.example.travelapp.navigation.AlbumDestination
import com.example.travelapp.navigation.DashboardDestination
import com.example.travelapp.navigation.ItineraryDestination
import com.example.travelapp.navigation.LoginDestination
import com.example.travelapp.navigation.MapDestination
import com.example.travelapp.navigation.PackingDestination
import com.example.travelapp.navigation.RegisterDestination
import com.example.travelapp.navigation.StatisticsDestination
import com.example.travelapp.navigation.TripDetailsDestination
import com.example.travelapp.navigation.TripListDestination
import com.example.travelapp.navigation.WeatherDestination
import com.example.travelapp.ui.screens.AddItineraryScreen
import com.example.travelapp.ui.screens.AddPackingItemScreen
import com.example.travelapp.ui.screens.AddTripScreen
import com.example.travelapp.ui.screens.AiSuggestionsScreen
import com.example.travelapp.ui.screens.AlbumScreen
import com.example.travelapp.ui.screens.DashboardScreen
import com.example.travelapp.ui.screens.ItineraryScreen
import com.example.travelapp.ui.screens.LoginScreen
import com.example.travelapp.ui.screens.MapScreen
import com.example.travelapp.ui.screens.PackingScreen
import com.example.travelapp.ui.screens.RegisterScreen
import com.example.travelapp.ui.screens.StatisticsScreen
import com.example.travelapp.ui.screens.TripDetailsScreen
import com.example.travelapp.ui.screens.TripListScreen
import com.example.travelapp.ui.screens.WeatherScreen
import com.example.travelapp.ui.stateholders.AuthViewModel

@Composable
fun TravelApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: LoginDestination.route

    val authViewModel: AuthViewModel = viewModel()

    Scaffold(
        containerColor = Color.Transparent
    ) {
        NavHost(
            navController = navController,
            startDestination = LoginDestination.route,
            modifier = Modifier.padding(it)
        ) {
            composable(route = LoginDestination.route) {
                LoginScreen(
                    modifier = modifier,
                    onRegisterClick = { navController.navigateToRegisterScreen() },
                    onLogin = { navController.navigateToDashboardScreen() },
                    authViewModel = authViewModel
                )
            }

            composable(route = RegisterDestination.route) {
                RegisterScreen(
                    modifier = modifier,
                    onLoginClick = { navController.navigateToLoginScreen() },
                    onRegister = { navController.navigateToDashboardScreen() },
                    authViewModel = authViewModel
                )
            }

            composable(route = DashboardDestination.route) {
                DashboardScreen(
                    modifier = modifier,
                    authViewModel = authViewModel,
                    onLogoutClick = { navController.navigateToLoginScreen() },
                    onFabClick = { navController.navigateToAddTripScreen() },
                    onTripsClick = { navController.navigateToTripListScreen() },
                    onMapClick = { navController.navigateToMapScreenFromDashboard() },
                    onStatisticsClick = { navController.navigateToStatisticsScreen() },
                    onAiSuggestionsClick = { navController.navigateToAiSuggestionsScreen() }
                )
            }

            composable(route = AddTripDestination.route) {
                AddTripScreen(
                    modifier = modifier,
                    authViewModel = authViewModel,
                    onLogoutClick = { navController.navigateToLoginScreen() },
                    onAddTrip = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(route = TripListDestination.route) {
                TripListScreen(
                    modifier = modifier,
                    authViewModel = authViewModel,
                    onLogoutClick = { navController.navigateToLoginScreen() },
                    onTripClick = { tripId -> navController.navigateToTripDetailsScreen(tripId) },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = TripDetailsDestination.routeWithArgs,
                arguments = TripDetailsDestination.arguments,
                deepLinks = listOf(TripDetailsDestination.deepLink)
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable

                TripDetailsScreen(
                    tripId = tripId,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = { navController.navigateToLoginScreen() },
                    onWeatherClick = { location -> navController.navigateToWeatherScreen(location) },
                    onMapClick = { navController.navigateToMapScreenFromTrip() },
                    onAlbumClick = { tripId -> navController.navigateToAlbumScreen(tripId) },
                    onItineraryClick = { tripId -> navController.navigateToItineraryScreen(tripId) },
                    onPackingClick = { tripId -> navController.navigateToPackingScreen(tripId) },
                    modifier = modifier,
                    authViewModel = authViewModel
                )
            }

            composable(
                route = WeatherDestination.routeWithArgs,
                arguments = WeatherDestination.arguments
            ) { backStackEntry ->
                val location = backStackEntry.arguments?.getString("location") ?: return@composable

                WeatherScreen(
                    location = location,
                    onBackClick = { navController.popBackStack() },
                    modifier = modifier
                )
            }

            composable(route = MapDestination.route) {
                MapScreen(
                    authViewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    modifier = modifier,
                    onLogoutClick = { navController.navigateToLoginScreen() }
                )
            }

            composable(
                route = AlbumDestination.routeWithArgs,
                arguments = AlbumDestination.arguments
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable

                AlbumScreen(
                    tripId = tripId,
                    modifier = Modifier,
                    authViewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = { navController.navigateToLoginScreen() }
                )
            }

            composable(
                route = ItineraryDestination.routeWithArgs,
                arguments = ItineraryDestination.arguments
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable

                ItineraryScreen(
                    tripId = tripId,
                    modifier = modifier,
                    authViewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    onAddItemClick = { tripId -> navController.navigateToAddItineraryScreen(tripId) },
                    onLogoutClick = { navController.navigateToLoginScreen() }
                )
            }

            composable(
                route = AddItineraryDestination.routeWithArgs,
                arguments = AddItineraryDestination.arguments
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable

                AddItineraryScreen(
                    tripId = tripId,
                    modifier = modifier,
                    authViewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = { navController.navigateToLoginScreen() },
                    onAddItem = { navController.popBackStack() }
                )
            }

            composable(
                route = PackingDestination.routeWithArgs,
                arguments = PackingDestination.arguments
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable

                PackingScreen(
                    tripId = tripId,
                    modifier = modifier,
                    authViewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    onAddItemClick = { tripId -> navController.navigateToAddPackingItemScreen(tripId) },
                    onLogoutClick = { navController.navigateToLoginScreen() }
                )
            }

            composable(
                route = AddPackingItemDestination.routeWithArgs,
                arguments = AddPackingItemDestination.arguments
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable

                AddPackingItemScreen(
                    tripId = tripId,
                    modifier = modifier,
                    authViewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = { navController.navigateToLoginScreen() },
                    onAddItem = { navController.popBackStack() }
                )
            }

            composable(route = StatisticsDestination.route) {
                StatisticsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = { navController.navigateToLoginScreen() },
                    modifier = modifier,
                    authViewModel = authViewModel
                )
            }

            composable(route = AiSuggestionsDestination.route) {
                AiSuggestionsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogoutClick = { navController.navigateToLoginScreen() },
                    modifier = modifier,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

private fun NavHostController.navigateToRegisterScreen() {
    this.navigate(RegisterDestination.route) {
        launchSingleTop = true
        popUpTo(LoginDestination.route) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToLoginScreen() {
    this.navigate(LoginDestination.route) {
        launchSingleTop = true
        popUpTo(LoginDestination.route) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToDashboardScreen() {
    this.navigate(DashboardDestination.route) {
        launchSingleTop = true
        popUpTo(LoginDestination.route) {
            inclusive = true
        }
    }
}

private fun NavHostController.navigateToAddTripScreen() {
    this.navigate(AddTripDestination.route) {
        launchSingleTop = true
        popUpTo(DashboardDestination.route) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToTripListScreen() {
    this.navigate(TripListDestination.route) {
        launchSingleTop = true
        popUpTo(DashboardDestination.route) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToTripDetailsScreen(tripId: Int) {
    this.navigate("${TripDetailsDestination.route}/$tripId") {
        launchSingleTop = true
        popUpTo(TripListDestination.route) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToWeatherScreen(location: String) {
    this.navigate("${WeatherDestination.route}/$location") {
        launchSingleTop = true
        popUpTo(TripDetailsDestination.routeWithArgs) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToMapScreenFromDashboard() {
    this.navigate(MapDestination.route) {
        launchSingleTop = true
        popUpTo(DashboardDestination.route) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToMapScreenFromTrip() {
    this.navigate(MapDestination.route) {
        launchSingleTop = true
        popUpTo(TripDetailsDestination.routeWithArgs) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToAlbumScreen(tripId: Int) {
    this.navigate("${AlbumDestination.route}/$tripId") {
        launchSingleTop = true
        popUpTo(TripDetailsDestination.routeWithArgs) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToItineraryScreen(tripId: Int) {
    this.navigate("${ItineraryDestination.route}/$tripId") {
        launchSingleTop = true
        popUpTo(TripDetailsDestination.routeWithArgs) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToAddItineraryScreen(tripId: Int) {
    this.navigate("${AddItineraryDestination.route}/$tripId") {
        launchSingleTop = true
        popUpTo(ItineraryDestination.routeWithArgs) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToPackingScreen(tripId: Int) {
    this.navigate("${PackingDestination.route}/$tripId") {
        launchSingleTop = true
        popUpTo(TripDetailsDestination.routeWithArgs) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToAddPackingItemScreen(tripId: Int) {
    this.navigate("${AddPackingItemDestination.route}/$tripId") {
        launchSingleTop = true
        popUpTo(PackingDestination.routeWithArgs) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToStatisticsScreen() {
    this.navigate(StatisticsDestination.route) {
        launchSingleTop = true
        popUpTo(DashboardDestination.route) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToAiSuggestionsScreen() {
    this.navigate(AiSuggestionsDestination.route) {
        launchSingleTop = true
        popUpTo(DashboardDestination.route) {
            inclusive = false
        }
    }
}