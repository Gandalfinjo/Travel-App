package com.example.travelapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travelapp.R
import com.example.travelapp.ui.viewmodels.AuthViewModel
import com.example.travelapp.ui.viewmodels.TripViewModel
import com.example.travelapp.database.models.enums.TripStatus
import com.example.travelapp.ui.elements.TripActionCard
import com.example.travelapp.ui.elements.TripActionRowCard
import com.example.travelapp.ui.elements.TripInfoCard

/**
* Displays detailed information about a specific trip
*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsScreen(
    tripId: Int,
    onBackClick: () -> Unit,
    onWeatherClick: (String) -> Unit,
    onMapClick: () -> Unit,
    onAlbumClick: (Int) -> Unit,
    onItineraryClick: (Int) -> Unit,
    onPackingClick: (Int) -> Unit,
    onExpensesClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    tripViewModel: TripViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val trip by tripViewModel.getTrip(tripId).collectAsState(initial = null)

    var showCancelDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.trip_details)) },
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
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(R.string.logout)
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        if (trip == null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val canCancelTrip = trip!!.status == TripStatus.PLANNED || trip!!.status == TripStatus.ONGOING

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                TripInfoCard(trip = trip!!)
            }

            item {
                Text(
                    text = stringResource(R.string.explore),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TripActionCard(
                        title = stringResource(R.string.weather),
                        subtitle = stringResource(R.string.current_forecast),
                        icon = Icons.Default.Cloud,
                        iconBgColor = Color(0xFFE6F1FB),
                        iconTintColor = Color(0xFF185FA5),
                        onClick = { onWeatherClick(trip!!.location) },
                        modifier = Modifier.weight(1f)
                    )
                    TripActionCard(
                        title = stringResource(R.string.map),
                        subtitle = stringResource(R.string.nearby_places),
                        icon = Icons.Default.NearMe,
                        iconBgColor = Color(0xFFE1F5EE),
                        iconTintColor = Color(0xFF0F6E56),
                        onClick = onMapClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TripActionCard(
                        title = stringResource(R.string.album),
                        subtitle = stringResource(R.string.trip_photos),
                        icon = Icons.Default.PhotoLibrary,
                        iconBgColor = Color(0xFFFBEAF0),
                        iconTintColor = Color(0xFF993556),
                        onClick = { onAlbumClick(tripId) },
                        modifier = Modifier.weight(1f)
                    )

                    TripActionCard(
                        title = stringResource(R.string.itinerary),
                        subtitle = stringResource(R.string.daily_plan),
                        icon = Icons.Default.Description,
                        iconBgColor = Color(0xFFFAEEDA),
                        iconTintColor = Color(0xFF854F0B),
                        onClick = { onItineraryClick(tripId) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                TripActionRowCard(
                    title = stringResource(R.string.packing_list),
                    subtitle = stringResource(R.string.manage_what_to_bring),
                    icon = Icons.Default.CheckCircle,
                    iconBgColor = Color(0xFFEEEDFE),
                    iconTintColor = Color(0xFF534AB7),
                    onClick = { onPackingClick(tripId) }
                )
            }

            item {
                TripActionRowCard(
                    title = stringResource(R.string.expenses),
                    subtitle = stringResource(R.string.track_your_spending),
                    icon = Icons.Default.AccountBalanceWallet,
                    iconBgColor = Color(0xFFEEEDFE),
                    iconTintColor = Color(0xFF534AB7),
                    onClick = { onExpensesClick(tripId) }
                )
            }

            if (canCancelTrip) {
                item {
                    Surface(
                        onClick = { showCancelDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp, 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.cancel_trip),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(text = stringResource(R.string.cancel_trip)) },
            text = { Text(text = stringResource(R.string.are_you_sure_you_want_to_cancel_the_trip)) },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    tripViewModel.cancelTrip(context, tripId)
                }) { Text(text = stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text(text = stringResource(R.string.no)) }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = stringResource(R.string.logout)) },
            text = { Text(text = stringResource(R.string.are_you_sure_you_want_to_logout)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                    }
                ) {
                    Text(text = stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(text = stringResource(R.string.no))
                }
            }
        )
    }
}