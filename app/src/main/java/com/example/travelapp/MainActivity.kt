package com.example.travelapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travelapp.navigation.DashboardDestination
import com.example.travelapp.navigation.LoginDestination
import com.example.travelapp.session.ThemePreference
import com.example.travelapp.ui.TravelApp
import com.example.travelapp.ui.theme.TravelAppTheme
import com.example.travelapp.ui.viewmodels.AuthViewModel
import com.example.travelapp.worker.utils.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createNotificationChannel(this)

        enableEdgeToEdge()
        setContent {
            val authViewModel: AuthViewModel = hiltViewModel()
            val authUiState by authViewModel.uiState.collectAsState()
            val themePreference by authViewModel.themePreference.collectAsState()

            LaunchedEffect(authUiState.loggedInUserId) {
                authUiState.loggedInUserId?.let {
                    authViewModel.syncTripStatuses(it)
                }
            }

            if (!authUiState.isSessionChecked) return@setContent

            val startDestination = if (authUiState.loggedInUser != null)
                DashboardDestination.route else LoginDestination.route

            val darkTheme = when (themePreference) {
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
            }

            TravelAppTheme(darkTheme = darkTheme) {
                Scaffold(
                    containerColor = Color.Transparent,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                )
                            )
                        )
                ) { innerPadding ->
                    TravelApp(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel,
                        startDestination = startDestination,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TravelAppTheme {
        TravelApp(
            authViewModel = hiltViewModel(),
            startDestination = LoginDestination.route
        )
    }
}