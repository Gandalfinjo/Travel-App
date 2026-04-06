package com.example.travelapp.ui

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.travelapp.R
import com.example.travelapp.navigation.AddExpenseDestination
import com.example.travelapp.navigation.AddItineraryDestination
import com.example.travelapp.navigation.AddPackingItemDestination
import com.example.travelapp.navigation.AddTripDestination
import com.example.travelapp.navigation.AiItineraryDestination
import com.example.travelapp.navigation.AiSuggestionsDestination
import com.example.travelapp.navigation.AlbumDestination
import com.example.travelapp.navigation.DashboardDestination
import com.example.travelapp.navigation.ExpenseDestination
import com.example.travelapp.navigation.ItineraryDestination
import com.example.travelapp.navigation.LoginDestination
import com.example.travelapp.navigation.MapDestination
import com.example.travelapp.navigation.PackingDestination
import com.example.travelapp.navigation.ProfileDestination
import com.example.travelapp.navigation.RegisterDestination
import com.example.travelapp.navigation.StatisticsDestination
import com.example.travelapp.navigation.TripDetailsDestination
import com.example.travelapp.navigation.TripListDestination
import com.example.travelapp.navigation.WeatherDestination
import com.example.travelapp.ui.screens.AddExpenseScreen
import com.example.travelapp.ui.screens.AddItineraryScreen
import com.example.travelapp.ui.screens.AddPackingItemScreen
import com.example.travelapp.ui.screens.AddTripScreen
import com.example.travelapp.ui.screens.AiItineraryScreen
import com.example.travelapp.ui.screens.AiSuggestionsScreen
import com.example.travelapp.ui.screens.AlbumScreen
import com.example.travelapp.ui.screens.DashboardScreen
import com.example.travelapp.ui.screens.ExpenseScreen
import com.example.travelapp.ui.screens.ItineraryScreen
import com.example.travelapp.ui.screens.LoginScreen
import com.example.travelapp.ui.screens.MapScreen
import com.example.travelapp.ui.screens.PackingScreen
import com.example.travelapp.ui.screens.ProfileScreen
import com.example.travelapp.ui.screens.RegisterScreen
import com.example.travelapp.ui.screens.StatisticsScreen
import com.example.travelapp.ui.screens.TripDetailsScreen
import com.example.travelapp.ui.screens.TripListScreen
import com.example.travelapp.ui.screens.WeatherScreen
import com.example.travelapp.ui.viewmodels.AuthViewModel
import com.example.travelapp.ui.viewmodels.TripViewModel

@Composable
fun TravelApp(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    startDestination: String
) {
    val authUiState by authViewModel.uiState.collectAsState()

    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: LoginDestination.route
    val currentSource = currentBackStackEntry?.arguments?.getString("source")

    val routesWithoutBottomNav = listOf(
        LoginDestination.route,
        RegisterDestination.route
    )

    val tripRoutes = listOf(
        TripListDestination.route,
        TripDetailsDestination.route,
        AddTripDestination.routeWithArgs,
        WeatherDestination.route,
        AlbumDestination.route,
        ItineraryDestination.route,
        AddItineraryDestination.route,
        PackingDestination.route,
        AddPackingItemDestination.route,
        ExpenseDestination.route,
        AddExpenseDestination.route
    )

    val showBottomNav = currentRoute !in routesWithoutBottomNav

    LaunchedEffect(authUiState.isLoggedIn) {
        if (authUiState.isLoggedIn) {
            navController.navigateToDashboardScreen()
        }
        else {
            navController.navigateToLoginScreen()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == DashboardDestination.route ||
                                (currentRoute.startsWith(TripDetailsDestination.route) && currentSource == "dashboard") ||
                                (currentRoute.startsWith(PackingDestination.route) && currentSource == "dashboard"),
                        onClick = { navController.navigateToDashboardScreen() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = stringResource(R.string.home)
                            )
                        },
                        label = { Text(text = stringResource(R.string.home)) }
                    )
                    NavigationBarItem(
                        selected = tripRoutes.any { currentRoute.startsWith(it) && currentSource != "stats" && currentSource != "ai" && currentSource != "dashboard" },
                        onClick = { navController.navigateToTripListScreen() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CardTravel,
                                contentDescription = stringResource(R.string.trips)
                            )
                        },
                        label = { Text(text = stringResource(R.string.trips)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == MapDestination.route,
                        onClick = { navController.navigateToMapScreenFromDashboard() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = stringResource(R.string.map)
                            )
                        },
                        label = { Text(text = stringResource(R.string.map)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == StatisticsDestination.route ||
                                (currentRoute.startsWith(ExpenseDestination.route) && currentSource == "stats"),
                        onClick = { navController.navigateToStatisticsScreen() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = stringResource(R.string.statistics)
                            )
                        },
                        label = { Text(text = stringResource(R.string.stats)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == AiSuggestionsDestination.route ||
                                (currentRoute.startsWith(AddTripDestination.routeWithArgs) && currentSource == "ai"),
                        onClick = { navController.navigateToAiSuggestionsScreen() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = stringResource(R.string.ai)
                            )
                        },
                        label = { Text(text = stringResource(R.string.ai)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = LoginDestination.route) {
                LoginScreen(
                    modifier = modifier,
                    onRegisterClick = { navController.navigateToRegisterScreen() },
                    authViewModel = authViewModel
                )
            }

            composable(route = RegisterDestination.route) {
                RegisterScreen(
                    modifier = modifier,
                    onLoginClick = { navController.navigateToLoginScreen() },
                    authViewModel = authViewModel
                )
            }

            composable(route = DashboardDestination.route) {
                DashboardScreen(
                    modifier = modifier,
                    authViewModel = authViewModel,
                    onTripClick = { tripId -> navController.navigateToTripDetailsScreenFromDashboard(tripId) },
                    onPackingClick = { tripId -> navController.navigateToPackingScreenFromDashboard(tripId) },
                    onItineraryClick = { tripId -> navController.navigateToItineraryScreen(tripId) },
                    onProfileClick = { navController.navigateToProfileScreen() }
                )
            }

            composable(
                route = AddTripDestination.routeWithArgs,
                arguments = AddTripDestination.arguments
            ) { backStackEntry ->
                AddTripScreen(
                    modifier = modifier,
                    authViewModel = authViewModel,
                    onAddTrip = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() },
                    prefillName = backStackEntry.arguments?.getString("destination"),
                    prefillDestination = backStackEntry.arguments?.getString("name"),
                    prefillBudget = backStackEntry.arguments?.getString("budget"),
                    prefillCurrency = backStackEntry.arguments?.getString("currency"),
                    prefillTransport = backStackEntry.arguments?.getString("transport")
                )
            }

            composable(route = TripListDestination.route) {
                TripListScreen(
                    modifier = modifier,
                    onTripClick = { tripId -> navController.navigateToTripDetailsScreenFromTripList(tripId) },
                    onFabClick = { navController.navigateToAddTripScreen() }
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
                    onWeatherClick = { location -> navController.navigateToWeatherScreen(location) },
                    onMapClick = { navController.navigateToMapScreenFromTrip() },
                    onAlbumClick = { tripId -> navController.navigateToAlbumScreen(tripId) },
                    onItineraryClick = { tripId -> navController.navigateToItineraryScreen(tripId) },
                    onPackingClick = { tripId -> navController.navigateToPackingScreenFromTripDetails(tripId) },
                    onExpensesClick = { tripId -> navController.navigateToExpenseScreenFromTripDetailsScreen(tripId)},
                    modifier = modifier
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
                    modifier = modifier,
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
                    onBackClick = { navController.popBackStack() }
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
                    onBackClick = { navController.popBackStack() },
                    onAddItemClick = { tripId -> navController.navigateToAddItineraryScreen(tripId) },
                    onAiItineraryClick = { navController.navigateToAiItineraryScreen(tripId) },
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
                    onBackClick = { navController.popBackStack() },
                    onAddItem = { navController.popBackStack() }
                )
            }

            composable(
                route = AiItineraryDestination.routeWithArgs,
                arguments = AiItineraryDestination.arguments
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable

                AiItineraryScreen(
                    tripId = tripId,
                    onBackClick = { navController.popBackStack() },
                    modifier = modifier
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
                    onBackClick = { navController.popBackStack() },
                    onAddItemClick = { tripId -> navController.navigateToAddPackingItemScreen(tripId) }
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
                    onBackClick = { navController.popBackStack() },
                    onAddItem = { navController.popBackStack() }
                )
            }

            composable(route = StatisticsDestination.route) {
                StatisticsScreen(
                    onExpensesClick = { tripId -> navController.navigateToExpenseScreenFromStatisticsScreen(tripId) },
                    modifier = modifier
                )
            }

            composable(route = AiSuggestionsDestination.route) {
                AiSuggestionsScreen(
                    onAddToTrips = { destination, name, budget, currency, transport ->
                        navController.navigateToAddTripScreenWithPrefill(destination, name, budget, currency, transport)
                    },
                    modifier = modifier
                )
            }

            composable(
                route = ExpenseDestination.routeWithArgs,
                arguments = ExpenseDestination.arguments
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable

                ExpenseScreen(
                    tripId = tripId,
                    onBackClick = { navController.popBackStack() },
                    onAddExpenseClick = { navController.navigateToAddExpenseScreen(tripId) },
                    modifier = modifier
                )
            }

            composable(
                route = AddExpenseDestination.routeWithArgs,
                arguments = AddExpenseDestination.arguments
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getInt("tripId") ?: return@composable

                val tripViewModel: TripViewModel = hiltViewModel()
                val trip by tripViewModel.getTrip(tripId).collectAsState(initial = null)

                trip?.let {
                    AddExpenseScreen(
                        tripId = tripId,
                        trip = it,
                        onBackClick = { navController.popBackStack() },
                        modifier = modifier
                    )
                }
            }

            composable(route = ProfileDestination.route) {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    modifier = modifier
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
        popUpTo(graph.startDestinationId) {
            inclusive = true
        }
    }
}

private fun NavHostController.navigateToDashboardScreen() {
    this.navigate(DashboardDestination.route) {
        launchSingleTop = true
        popUpTo(graph.startDestinationId) {
            inclusive = true
        }
    }
}

private fun NavHostController.navigateToAddTripScreen() {
    this.navigate(
        AddTripDestination.routeWithArgs
        .replace("{source}", "trips")
        .replace("{destination}", "")
        .replace("{name}", "")
        .replace("{budget}", "")
        .replace("{currency}", "")
        .replace("{transport}", "")
    ) {
        launchSingleTop = true
        popUpTo(TripListDestination.route) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToAddTripScreenWithPrefill(
    destination: String,
    name: String,
    budget: String,
    currency: String,
    transport: String
) {
    this.navigate(
    AddTripDestination.routeWithArgs
        .replace("{source}", "ai")
        .replace("{destination}", Uri.encode(destination))
        .replace("{name}", Uri.encode(name))
        .replace("{budget}", Uri.encode(budget))
        .replace("{currency}", Uri.encode(currency))
        .replace("{transport}", Uri.encode(transport))
    ) {
        launchSingleTop = true
        popUpTo(AiSuggestionsDestination.route) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToTripListScreen() {
    this.navigate(TripListDestination.route) {
        launchSingleTop = true
        popUpTo(DashboardDestination.route) {
            inclusive = false
            saveState = true
        }
        restoreState = true
    }
}

private fun NavHostController.navigateToTripDetailsScreenFromTripList(tripId: Int) {
    this.navigate("${TripDetailsDestination.route}/$tripId?source=trips") {
        launchSingleTop = true
        popUpTo(TripListDestination.route) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToTripDetailsScreenFromDashboard(tripId: Int) {
    this.navigate("${TripDetailsDestination.route}/$tripId?source=dashboard") {
        launchSingleTop = true
        popUpTo(DashboardDestination.route) {
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
            saveState = true
        }
        restoreState = true
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

private fun NavHostController.navigateToAiItineraryScreen(tripId: Int) {
    this.navigate("${AiItineraryDestination.route}/$tripId") {
        launchSingleTop = true
        popUpTo(ItineraryDestination.routeWithArgs) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToPackingScreenFromTripDetails(tripId: Int) {
    this.navigate("${PackingDestination.route}/$tripId?source=trip") {
        launchSingleTop = true
        popUpTo(TripDetailsDestination.routeWithArgs) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToPackingScreenFromDashboard(tripId: Int) {
    this.navigate("${PackingDestination.route}/$tripId?source=dashboard") {
        launchSingleTop = true
        popUpTo(DashboardDestination.route) {
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
            saveState = true
        }
        restoreState = true
    }
}

private fun NavHostController.navigateToAiSuggestionsScreen() {
    this.navigate(AiSuggestionsDestination.route) {
        launchSingleTop = true
        popUpTo(DashboardDestination.route) {
            inclusive = false
            saveState = true
        }
        restoreState = true
    }
}

private fun NavHostController.navigateToExpenseScreenFromTripDetailsScreen(tripId: Int) {
    this.navigate("${ExpenseDestination.route}/$tripId?source=trips") {
        launchSingleTop = true
        popUpTo(TripDetailsDestination.routeWithArgs) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToExpenseScreenFromStatisticsScreen(tripId: Int) {
    this.navigate("${ExpenseDestination.route}/$tripId?source=stats") {
        launchSingleTop = true
        popUpTo(StatisticsDestination.route) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToAddExpenseScreen(tripId: Int) {
    this.navigate("${AddExpenseDestination.route}/$tripId") {
        launchSingleTop = true
        popUpTo(ExpenseDestination.routeWithArgs) {
            inclusive = false
        }
    }
}

private fun NavHostController.navigateToProfileScreen() {
    this.navigate(ProfileDestination.route) {
        launchSingleTop = true
        popUpTo(DashboardDestination.route) {
            inclusive = false
        }
    }
}