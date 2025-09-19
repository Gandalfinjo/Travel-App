package com.example.travelapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.travelapp.R
import com.example.travelapp.ui.stateholders.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    location: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {
    LaunchedEffect(location) {
        weatherViewModel.loadCurrentWeather(location)
    }

    val uiState by weatherViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Weather") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                        Text(
                            text = stringResource(R.string.error, uiState.error!!),
                            color = Color.Red
                        )
                }
                uiState.weather != null -> {
                    val weather = uiState.weather!!
                    val iconCode = weather.weather.firstOrNull()?.icon ?: "01d"
                    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@4x.png"

                    val cardColor = when {
                        iconCode.startsWith("01") -> Color(0xFFFFF9C4) // sunčano (clear sky)
                        iconCode.startsWith("02") || iconCode.startsWith("03") || iconCode.startsWith(
                            "04"
                        ) -> Color(0xFFB0BEC5) // oblačno
                        iconCode.startsWith("09") || iconCode.startsWith("10") -> Color(0xFF81D4FA) // kiša
                        iconCode.startsWith("11") -> Color(0xFFFFAB91) // oluja
                        iconCode.startsWith("13") -> Color(0xFFE1F5FE) // sneg
                        iconCode.startsWith("50") -> Color(0xFFBCAAA4) // magla
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = cardColor
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = weather.name,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            AsyncImage(
                                model = iconUrl,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp)
                            )

                            Text(
                                text = "${weather.main.temp}°C",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = weather.weather.joinToString { it.description },
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = "Feels like: ${weather.main.feelsLike}°C")
                            Text(text = "Humidity: ${weather.main.humidity}%")
                            Text(text = "Wind: ${weather.wind.speed} m/s")
                        }
                    }
                }
            }
        }
    }
}