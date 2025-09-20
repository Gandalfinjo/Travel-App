package com.example.travelapp.ui.screens

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travelapp.R
import com.example.travelapp.database.converters.TravelTypeConverters
import com.example.travelapp.database.models.enums.TransportType
import com.example.travelapp.ui.stateholders.AuthViewModel
import com.example.travelapp.ui.stateholders.TripViewModel
import com.example.travelapp.ui.theme.TravelAppTheme
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    tripViewModel: TripViewModel = hiltViewModel(),
    onLogoutClick: () -> Unit,
    onAddTrip: () -> Unit
) {
    val context = LocalContext.current

    val authUiState by authViewModel.uiState.collectAsState()
    val uiState by tripViewModel.uiState.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var transport by remember { mutableStateOf(TransportType.OTHER) }
    var budget by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    var expandedTransport by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var showAlert by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.add_trip)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = stringResource(R.string.trip_name)) }
            )

            Spacer(modifier = Modifier.padding(4.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(text = stringResource(R.string.description)) },
                singleLine = false,
                minLines = 2,
                modifier = Modifier.fillMaxWidth(0.6575f)
            )

            Spacer(modifier = Modifier.padding(4.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text(text = stringResource(R.string.location)) }
            )

            Spacer(modifier = Modifier.padding(4.dp))

            ExposedDropdownMenuBox(
                expanded = expandedTransport,
                onExpandedChange = { expandedTransport = !expandedTransport }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = transport.name,
                    onValueChange = {},
                    label = { Text(text = stringResource(R.string.transport)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTransport) },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true)
                )

                ExposedDropdownMenu(
                    expanded = expandedTransport,
                    onDismissRequest = { expandedTransport = false }
                ) {
                    TransportType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                transport = type
                                expandedTransport = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(4.dp))

            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it },
                label = { Text(text = stringResource(R.string.budget)) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                )
            )

            Spacer(modifier = Modifier.padding(4.dp))

            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                label = { Text(text = stringResource(R.string.currency)) }
            )

            Spacer(modifier = Modifier.padding(4.dp))

            OutlinedTextField(
                readOnly = true,
                value = startDate?.let { dateFormatter.format(it) } ?: "",
                onValueChange = { },
                label = { Text(text = stringResource(R.string.start_date)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null
                    )
                },
                modifier = Modifier.pointerInput(startDate) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (upEvent != null) {
                            showStartDatePicker = true
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.padding(4.dp))

            OutlinedTextField(
                readOnly = true,
                value = endDate?.let { dateFormatter.format(it) } ?: "",
                onValueChange = {  },
                label = { Text(text = stringResource(R.string.end_date)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null
                    )
                },
                modifier = Modifier.pointerInput(endDate) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (upEvent != null) {
                            showEndDatePicker = true
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.padding(4.dp))

            Button(
                onClick = {
                    if (name == "" || location == "" || budget == "" || currency == "" || startDate == null || endDate == null) {
                        tripViewModel.setErrorMessage("Missing fields")
                        showAlert = true
                        return@Button
                    }

                    startDate?.let { start ->
                        endDate?.let { end ->
                            if (start > end) {
                                tripViewModel.setErrorMessage("You cannot put the Start Date after the End Date")
                                showAlert = true
                                return@Button
                            }
                        }
                    }

                    tripViewModel.addTrip(
                        name,
                        description,
                        location,
                        transport,
                        budget.toDoubleOrNull() ?: 0.0,
                        currency,
                        TravelTypeConverters().fromTimestampMillis(startDate)!!,
                        TravelTypeConverters().fromTimestampMillis(endDate)!!,
                        authUiState.loggedInUserId!!,
                        context
                    )

                    onAddTrip()
                }
            ) {
                Text(text = stringResource(R.string.add))
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = stringResource(R.string.logout)) },
            text = { Text(text = "Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        onLogoutClick()
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

    if (showAlert) {
        AlertDialog(
            onDismissRequest = {
                showAlert = false
                tripViewModel.clearError()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAlert = false
                        tripViewModel.clearError()
                    }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            },
            title = { Text(text = stringResource(R.string.add_a_trip)) },
            text = { uiState.errorMessage?.let { Text(it) } }
        )
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDate = startDatePickerState.selectedDateMillis ?: startDate
                        showStartDatePicker = false
                    }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        ) {
            DatePicker(
                state = startDatePickerState
            )
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDate = endDatePickerState.selectedDateMillis ?: endDate
                        showEndDatePicker = false
                    }
                ) {
                    Text(text = stringResource(R.string.ok))
                }
            }
        ) {
            DatePicker(
                state = endDatePickerState
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTripPreview() {
    TravelAppTheme {
        AddTripScreen(
            onLogoutClick = {},
            onAddTrip = {}
        )
    }
}