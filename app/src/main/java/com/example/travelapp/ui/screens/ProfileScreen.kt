package com.example.travelapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.travelapp.R
import com.example.travelapp.ui.elements.ProfileInfoRow
import com.example.travelapp.ui.elements.SectionLabel
import com.example.travelapp.ui.elements.StatMetricCard
import com.example.travelapp.ui.elements.UserAvatar
import com.example.travelapp.ui.viewmodels.AuthViewModel
import com.example.travelapp.ui.viewmodels.ProfileViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val uiState by profileViewModel.uiState.collectAsState()

    var showAvatarSheet by remember { mutableStateOf(false) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val avatarSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            profileViewModel.updateProfilePicture(it.toString())
            authViewModel.updateProfilePicture(it.toString())
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraPhotoUri?.let { uri ->
                profileViewModel.updateProfilePicture(uri.toString())
                authViewModel.updateProfilePicture(uri.toString())
            }
        }
    }

    var showEditName by remember { mutableStateOf(false) }
    var showEditEmail by remember { mutableStateOf(false) }
    var showEditUsername by remember { mutableStateOf(false) }
    var showEditPassword by remember { mutableStateOf(false) }

//    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
//        if (uiState.successMessage != null || uiState.errorMessage != null) {
//            profileViewModel.clearMessages()
//        }
//    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            profileViewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            profileViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile),
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
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
        else {
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
                            UserAvatar(
                                firstname = uiState.firstname,
                                lastname = uiState.lastname,
                                profilePicturePath = uiState.profilePicturePath,
                                size = 72.dp,
                                showEditBadge = true,
                                onClick = { showAvatarSheet = true }
                            )

                            Text(
                                text = "${uiState.firstname} ${uiState.lastname}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "@${uiState.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    SectionLabel(stringResource(R.string.overview))
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatMetricCard(
                            label = stringResource(R.string.total_trips),
                            value = uiState.totalTrips.toString(),
                            modifier = Modifier.weight(1f)
                        )

                        StatMetricCard(
                            label = stringResource(R.string.destinations),
                            value = uiState.uniqueDestinations.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    SectionLabel(stringResource(R.string.personal_info))
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            ProfileInfoRow(
                                label = stringResource(R.string.first_name),
                                value = uiState.firstname,
                                onEditClick = { showEditName = true }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = 0.5.dp
                            )

                            ProfileInfoRow(
                                label = stringResource(R.string.last_name),
                                value = uiState.lastname,
                                onEditClick = { showEditName = true }
                            )
                        }
                    }
                }

                item {
                    SectionLabel(stringResource(R.string.account_info))
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            ProfileInfoRow(
                                label = stringResource(R.string.username),
                                value = uiState.username,
                                onEditClick = { showEditUsername = true }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = 0.5.dp
                            )

                            ProfileInfoRow(
                                label = stringResource(R.string.email),
                                value = uiState.email,
                                onEditClick = { showEditEmail = true }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                thickness = 0.5.dp
                            )

                            ProfileInfoRow(
                                label = stringResource(R.string.password),
                                value = "••••••••",
                                onEditClick = { showEditPassword = true }
                            )
                        }
                    }
                }

                item {
                    SectionLabel(stringResource(R.string.preferences))
                }

                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            ProfileInfoRow(
                                label = stringResource(R.string.default_currency),
                                value = "EUR",
                                onEditClick = { /* TODO */ }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAvatarSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAvatarSheet = false },
            sheetState = avatarSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_picture),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                HorizontalDivider(thickness = 0.5.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            showAvatarSheet = false
                            galleryLauncher.launch("image/*")
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = stringResource(R.string.choose_from_gallery),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            showAvatarSheet = false
                            val file = File(
                                context.filesDir,
                                "profile_${System.currentTimeMillis()}.jpg"
                            )

                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )

                            cameraPhotoUri = uri
                            cameraLauncher.launch(uri)
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = stringResource(R.string.take_a_photo),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (uiState.profilePicturePath != null) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 0.5.dp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                showAvatarSheet = false
                                profileViewModel.updateProfilePicture(null)
                                authViewModel.updateProfilePicture(null)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        Text(
                            text = stringResource(R.string.remove_photo),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    if (showEditName) {
        var firstname by remember { mutableStateOf(uiState.firstname) }
        var lastname by remember { mutableStateOf(uiState.lastname) }

        AlertDialog(
            onDismissRequest = { showEditName = false },
            title = { Text(text = stringResource(R.string.edit_name)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = firstname,
                        onValueChange = { firstname = it },
                        label = { Text(text = stringResource(R.string.first_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = lastname,
                        onValueChange = { lastname = it },
                        label = { Text(stringResource(R.string.last_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    profileViewModel.updateName(firstname, lastname)
                    showEditName = false
                }) { Text(text = stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditName = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showEditEmail) {
        var email by remember { mutableStateOf(uiState.email) }

        AlertDialog(
            onDismissRequest = { showEditEmail = false },
            title = { Text(text = stringResource(R.string.edit_email)) },
            text = {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(text = stringResource(R.string.email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    profileViewModel.updateEmail(email)
                    showEditEmail = false
                }) { Text(text = stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditEmail = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showEditUsername) {
        var username by remember { mutableStateOf(uiState.username) }

        AlertDialog(
            onDismissRequest = { showEditUsername = false },
            title = { Text(text = stringResource(R.string.edit_username)) },
            text = {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(text = stringResource(R.string.username)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    profileViewModel.updateUsername(username)
                    showEditUsername = false
                }) { Text(text = stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditUsername = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showEditPassword) {
        var current by remember { mutableStateOf("") }
        var new by remember { mutableStateOf("") }
        var confirm by remember { mutableStateOf("") }
        var currentVisible by remember { mutableStateOf(false) }
        var newVisible by remember { mutableStateOf(false) }
        var confirmVisible by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditPassword = false },
            title = { Text(text = stringResource(R.string.change_password)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = current,
                        onValueChange = { current = it },
                        label = { Text(text = stringResource(R.string.current_password)) },
                        singleLine = true,
                        visualTransformation = if (currentVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { currentVisible = !currentVisible }) {
                                Icon(
                                    imageVector = if (currentVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = new,
                        onValueChange = { new = it },
                        label = { Text(text = stringResource(R.string.new_password)) },
                        singleLine = true,
                        visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newVisible = !newVisible }) {
                                Icon(
                                    imageVector = if (newVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { confirm = it },
                        label = { Text(stringResource(R.string.confirm_password)) },
                        singleLine = true,
                        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(
                                    imageVector = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    profileViewModel.updatePassword(current, new, confirm)
                    showEditPassword = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditPassword = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}