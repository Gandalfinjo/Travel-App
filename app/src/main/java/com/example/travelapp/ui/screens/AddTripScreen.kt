package com.example.travelapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.travelapp.database.models.enums.TransportType
import com.example.travelapp.ui.viewmodels.AuthViewModel
import com.example.travelapp.ui.viewmodels.TripViewModel
import com.example.travelapp.ui.theme.TravelAppTheme
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Screen for adding a new trip with validation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    tripViewModel: TripViewModel = hiltViewModel(),
    onAddTrip: () -> Unit,
    onBackClick: () -> Unit,
    prefillDestination: String? = null,
    prefillName: String? = null,
    prefillBudget: String? = null,
    prefillCurrency: String? = null,
    prefillTransport: String? = null
) {
    val context = LocalContext.current

    val authUiState by authViewModel.uiState.collectAsState()
    val uiState by tripViewModel.uiState.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(prefillName ?: "") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf(prefillDestination ?: "") }
    var transport by remember {
        mutableStateOf(
            prefillTransport?.let {
                runCatching { TransportType.valueOf(it) }.getOrDefault(TransportType.OTHER)
            } ?: TransportType.OTHER
        )
    }
    var budget by remember { mutableStateOf(prefillBudget ?: "") }
    var currency by remember { mutableStateOf(prefillCurrency ?: "") }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    var expandedTransport by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var showAlert by remember { mutableStateOf(false) }

    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) showAlert = true
    }

    LaunchedEffect(uiState.tripAddedSuccessfully) {
        if (uiState.tripAddedSuccessfully) {
            tripViewModel.resetAddTripState()
            onAddTrip()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.add_trip)) },
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
                            contentDescription = null
                        )
                    }
                }
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.basic_info),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.trip_name),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text(text = stringResource(R.string.e_g_japan_2025)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.location),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            placeholder = { Text(text = stringResource(R.string.e_g_tokyo_japan)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.description),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text(text = stringResource(R.string.description_optional)) },
                            singleLine = false,
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.dates),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.start_date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                readOnly = true,
                                value = startDate?.let { dateFormatter.format(it) } ?: "",
                                onValueChange = {},
                                placeholder = { Text(text = stringResource(R.string.dd_mm_yyyy)) },
                                trailingIcon = {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(startDate) {
                                        awaitEachGesture {
                                            awaitFirstDown(pass = PointerEventPass.Initial)
                                            val upEvent =
                                                waitForUpOrCancellation(pass = PointerEventPass.Initial)
                                            if (upEvent != null) showStartDatePicker = true
                                        }
                                    }
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.end_date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                readOnly = true,
                                value = endDate?.let { dateFormatter.format(it) } ?: "",
                                onValueChange = {},
                                placeholder = { Text(text = stringResource(R.string.dd_mm_yyyy)) },
                                trailingIcon = {
                                    Icon(Icons.Default.DateRange, contentDescription = null)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(endDate) {
                                        awaitEachGesture {
                                            awaitFirstDown(pass = PointerEventPass.Initial)
                                            val upEvent =
                                                waitForUpOrCancellation(pass = PointerEventPass.Initial)
                                            if (upEvent != null) showEndDatePicker = true
                                        }
                                    }
                            )
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.transport),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedTransport,
                        onExpandedChange = { expandedTransport = !expandedTransport }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = transport.name,
                            onValueChange = {},
                            placeholder = { Text(text = stringResource(R.string.select_transport)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTransport)
                            },
                            modifier = Modifier
                                .menuAnchor(
                                    type = MenuAnchorType.PrimaryNotEditable,
                                    enabled = true
                                )
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedTransport,
                            onDismissRequest = { expandedTransport = false }
                        ) {
                            TransportType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(text = type.name) },
                                    onClick = {
                                        transport = type
                                        expandedTransport = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.budget),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.budget),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = budget,
                                onValueChange = { budget = it.filter { c -> c.isDigit() || c == '.' } },
                                placeholder = { Text(text = "0.00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(
                            modifier = Modifier.width(90.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.currency),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = currency,
                                onValueChange = { if (it.length <= 3) currency = it.uppercase() },
                                placeholder = { Text(text = stringResource(R.string.eur)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    tripViewModel.addTrip(
                        name, description, location, transport,
                        budget, currency,
                        startDateMillis = startDate,
                        endDateMillis = endDate,
                        userId = authUiState.loggedInUserId!!,
                        context
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.add),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))
        }
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
                ) { Text(text = stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text(text = stringResource(R.string.no)) }
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
                TextButton(onClick = {
                    showAlert = false
                    tripViewModel.clearError()
                }) { Text(text = stringResource(R.string.ok)) }
            },
            title = { Text(text = stringResource(R.string.add_a_trip)) },
            text = { uiState.errorMessage?.let { Text(text = it) } }
        )
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDate = startDatePickerState.selectedDateMillis ?: startDate
                    showStartDatePicker = false
                }) { Text(text = stringResource(R.string.ok)) }
            }
        ) { DatePicker(state = startDatePickerState) }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDate = endDatePickerState.selectedDateMillis ?: endDate
                    showEndDatePicker = false
                }) { Text(text = stringResource(R.string.ok)) }
            }
        ) { DatePicker(state = endDatePickerState) }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTripPreview() {
    TravelAppTheme {
        AddTripScreen(
            onAddTrip = {},
            onBackClick = {}
        )
    }
}