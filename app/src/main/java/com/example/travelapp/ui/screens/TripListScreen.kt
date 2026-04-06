package com.example.travelapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travelapp.R
import com.example.travelapp.database.models.enums.TripStatus
import com.example.travelapp.ui.elements.SectionLabel
import com.example.travelapp.ui.elements.TripCard
import com.example.travelapp.ui.viewmodels.TripViewModel

/**
 * Displays a scrollable list of all user trips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    modifier: Modifier = Modifier,
    tripViewModel: TripViewModel = hiltViewModel(),
    onTripClick: (Int) -> Unit,
    onFabClick: () -> Unit
) {
    val tripUiState by tripViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.my_trips),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.new_trip)
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        if (tripUiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else if (tripUiState.trips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardTravel,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = stringResource(R.string.no_trips_yet),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = stringResource(R.string.tap_the_button_to_add_your_first_trip),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp, top = 4.dp)
            ) {
                val ongoing = tripUiState.trips.filter { it.status == TripStatus.ONGOING }
                val planned = tripUiState.trips.filter { it.status == TripStatus.PLANNED }
                val finished = tripUiState.trips.filter { it.status == TripStatus.FINISHED }
                val cancelled = tripUiState.trips.filter { it.status == TripStatus.CANCELLED }

                if (ongoing.isNotEmpty()) {
                    item {
                        SectionLabel(stringResource(R.string.ongoing_trip))
                    }

                    items(ongoing, key = { it.id }) { trip ->
                        TripCard(trip = trip, onClick = { onTripClick(trip.id) })
                    }
                }

                if (planned.isNotEmpty()) {
                    item {
                        SectionLabel(stringResource(R.string.planned))
                    }

                    items(planned, key = { it.id }) { trip ->
                        TripCard(trip = trip, onClick = { onTripClick(trip.id) })
                    }
                }

                if (finished.isNotEmpty()) {
                    item {
                        SectionLabel(stringResource(R.string.finished))
                    }

                    items(finished, key = { it.id }) { trip ->
                        TripCard(trip = trip, onClick = { onTripClick(trip.id) })
                    }
                }

                if (cancelled.isNotEmpty()) {
                    item {
                        SectionLabel(stringResource(R.string.cancelled))
                    }

                    items(cancelled, key = { it.id }) { trip ->
                        TripCard(trip = trip, onClick = { onTripClick(trip.id) })
                    }
                }
            }
        }
    }
}