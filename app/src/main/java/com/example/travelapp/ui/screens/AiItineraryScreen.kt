package com.example.travelapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travelapp.R
import com.example.travelapp.database.models.enums.TripStatus
import com.example.travelapp.ui.viewmodels.AiItineraryViewModel
import com.example.travelapp.ui.viewmodels.TripViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Screen for generating the itinerary with AI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiItineraryScreen(
    tripId: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    tripViewModel: TripViewModel = hiltViewModel(),
    aiItineraryViewModel: AiItineraryViewModel = hiltViewModel()
) {
    val trip by tripViewModel.getTrip(tripId).collectAsState(initial = null)
    val uiState by aiItineraryViewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.addedSuccessfully) {
        if (uiState.addedSuccessfully) {
            aiItineraryViewModel.resetAddedSuccessfully()
            onBackClick()
        }
    }

    LaunchedEffect(tripId) {
        aiItineraryViewModel.loadExistingItems(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.ai_itinerary)) },
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
        if (trip == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
        else {
            val datePickerState = rememberDatePickerState(
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val pickedDate = Instant.ofEpochMilli(utcTimeMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        val startLimit = if (trip!!.status == TripStatus.ONGOING)
                            LocalDate.now() else trip!!.startDate

                        return !pickedDate.isBefore(startLimit) && !pickedDate.isAfter(trip!!.endDate)
                    }
                }
            )

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.select_day),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(Modifier.width(8.dp))

                                Text(
                                    text = uiState.selectedDate?.toString() ?: stringResource(R.string.choose_a_date)
                                )
                            }

                            Text(
                                text = if (trip!!.status == TripStatus.ONGOING)
                                    stringResource(
                                        R.string.you_can_generate_itinerary_from_today_to,
                                        LocalDate.now(),
                                        trip!!.endDate
                                    )
                                    else stringResource(
                                    R.string.only_dates_within_the_trip_are_selectable,
                                    trip!!.startDate,
                                    trip!!.endDate
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = { aiItineraryViewModel.generateItinerary(trip!!.location) },
                                enabled = uiState.selectedDate != null && !uiState.isLoading,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(Modifier.width(8.dp))

                                Text(text = stringResource(R.string.generate_itinerary))
                            }
                        }
                    }
                }

                if (uiState.isLoading) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp)
                        ) {
                            CircularProgressIndicator()

                            Text(
                                text = stringResource(R.string.ai_is_thinking),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (uiState.errorMessage != null) {
                    item {
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                if (uiState.suggestions.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.select_activities_to_add),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    itemsIndexed(uiState.suggestions) { index, item ->
                        Card(
                            onClick = { aiItineraryViewModel.toggleItemSelection(index) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (item.isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = if (item.isSelected) 1.dp else 0.5.dp,
                                color = if (item.isSelected)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Checkbox(
                                    checked = item.isSelected,
                                    onCheckedChange = { aiItineraryViewModel.toggleItemSelection(index) }
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    if (item.description.isNotBlank()) {
                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { aiItineraryViewModel.addSelectedItems(tripId) },
                            enabled = uiState.suggestions.any { it.isSelected },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.add_selected,
                                    uiState.suggestions.count { it.isSelected }),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                aiItineraryViewModel.onDateSelected(date)
                            }
                            showDatePicker = false
                        }) { Text(text = stringResource(R.string.ok)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text(text = stringResource(R.string.cancel)) }
                    }
                ) { DatePicker(state = datePickerState) }
            }
        }
    }
}