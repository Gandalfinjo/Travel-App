package com.example.travelapp.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.travelapp.R
import com.example.travelapp.database.models.Photo
import com.example.travelapp.database.models.enums.TripStatus
import com.example.travelapp.ui.elements.ZoomableImage
import com.example.travelapp.ui.viewmodels.PhotoViewModel
import com.example.travelapp.ui.viewmodels.TripViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Photo album screen for viewing and adding trip photos.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AlbumScreen(
    tripId: Int,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    tripViewModel: TripViewModel = hiltViewModel(),
    photoViewModel: PhotoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val photos by photoViewModel.getTripPhotos(tripId).collectAsState(initial = emptyList())
    val pagerState = rememberPagerState(pageCount = { photos.size })
    val trip by tripViewModel.getTrip(tripId).collectAsState(initial = null)

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var selectedPhotoIndex by remember { mutableStateOf<Int?>(null) }

    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val launchCamera by photoViewModel.launchCamera.collectAsState()
    val resolvableException by photoViewModel.resolvableException.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            // Try to read the actual date taken from the photo metadata
            val dateTaken = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.DATE_TAKEN),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                    if (index != -1) cursor.getLong(index) else null
                } else null
            } ?: System.currentTimeMillis()

            val exif = context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream)
            }

            val latLong = exif?.latLong

            photoViewModel.addPhoto(Photo(
                filePath = it.toString(),
                tripId = tripId,
                dateTaken = dateTaken,
                latitude = latLong?.get(0),
                longitude = latLong?.get(1)
            ))
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraPhotoUri?.let { uri ->
                val hasPermission = ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    LocationServices.getFusedLocationProviderClient(context).getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    ).addOnSuccessListener { location ->
                        photoViewModel.addPhoto(Photo(
                            filePath = uri.toString(),
                            tripId = tripId,
                            dateTaken = System.currentTimeMillis(),
                            latitude = location?.latitude,
                            longitude = location?.longitude
                        ))
                    }
                }
                else {
                    photoViewModel.addPhoto(
                        Photo(
                            filePath = uri.toString(),
                            tripId = tripId,
                            dateTaken = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { _ ->
        val file = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        cameraPhotoUri = uri
        cameraLauncher.launch(uri)
    }

    LaunchedEffect(resolvableException) {
        resolvableException?.let { exception ->
            locationSettingsLauncher.launch(
                IntentSenderRequest.Builder(exception.resolution.intentSender).build()
            )
            photoViewModel.clearResolvableException()
        }
    }

    LaunchedEffect(launchCamera) {
        if (launchCamera) {
            val file = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            cameraPhotoUri = uri
            cameraLauncher.launch(uri)
            photoViewModel.onCameraLaunched()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.photo_album),
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
        }
        else {
            val canAddPhotos = trip!!.status == TripStatus.ONGOING || trip!!.status == TripStatus.FINISHED

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
                                text = stringResource(R.string.add_photos),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    enabled = canAddPhotos,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Text(text = stringResource(R.string.gallery))
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (locationPermission.status.isGranted) {
                                            photoViewModel.checkLocationSettingsAndPrepareCamera()
                                        }
                                        else {
                                            locationPermission.launchPermissionRequest()
                                        }
                                    },
                                    enabled = canAddPhotos,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Text(text = stringResource(R.string.camera))
                                }
                            }

                            if (!canAddPhotos) {
                                Text(
                                    text = stringResource(R.string.photos_can_be_added_only_for_ongoing_or_finished_trips),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (photos.isEmpty()) {
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
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = stringResource(R.string.no_photos_yet),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    text = stringResource(R.string.add_photos_to_remember_your_trip),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                else {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.photos, photos.size),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )

                                HorizontalPager(
                                    state = pagerState,
                                    pageSize = PageSize.Fill,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                ) { page ->
                                    val photo = photos[page]
                                    AsyncImage(
                                        model = photo.filePath.toUri(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { selectedPhotoIndex = page }
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    photos.forEachIndexed { index, _ ->
                                        Box(
                                            modifier = Modifier
                                                .padding(2.dp)
                                                .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                                                .background(
                                                    color = if (pagerState.currentPage == index)
                                                        MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.outlineVariant,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedPhotoIndex != null) {
        val dialogPagerState = rememberPagerState(
            initialPage = selectedPhotoIndex!!,
            pageCount = { photos.size }
        )

        val currentPhoto = photos[dialogPagerState.currentPage]
        val isCameraPhoto = currentPhoto.filePath.startsWith("content://${context.packageName}.provider")

        var showDeleteDialog by remember { mutableStateOf(false) }

        val savedPhotoIds by photoViewModel.savedPhotoIds.collectAsState()
        val savedToGallery = savedPhotoIds.contains(currentPhoto.id)

        var isEditingDescription by remember { mutableStateOf(false) }
        var descriptionDraft by remember(dialogPagerState.currentPage) {
            mutableStateOf(currentPhoto.description ?: "")
        }

        Dialog(
            onDismissRequest = { selectedPhotoIndex = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            var isZoomed by remember { mutableStateOf(false) }

            LaunchedEffect(dialogPagerState.currentPage) {
                isZoomed = false
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                HorizontalPager(
                    state = dialogPagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = !isZoomed
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        ZoomableImage(
                            model = photos[page].filePath.toUri(),
                            modifier = Modifier.fillMaxSize(),
                            onScaleChanged = { isZoomed = it > 1f }
                        )

                        photos[page].description?.takeIf { it.isNotBlank() }?.let { caption ->
                            Text(
                                text = caption,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(start = 16.dp, end = 16.dp, bottom = 90.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedPhotoIndex = null },
                            modifier = Modifier.background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "${dialogPagerState.currentPage + 1} / ${photos.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )

                        Spacer(Modifier.size(48.dp))
                    }

                    val formattedDate = remember(currentPhoto.dateTaken) {
                        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                            .format(Date(currentPhoto.dateTaken))
                    }

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isCameraPhoto) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = {
                                    if (!savedToGallery) {
                                        photoViewModel.savePhotoToGallery(
                                            contentResolver = context.contentResolver,
                                            photo = currentPhoto,
                                        )
                                    }
                                },
                                modifier = Modifier.background(
                                    color = if (savedToGallery)
                                        Color.White.copy(alpha = 0.1f)
                                    else Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                            ) {
                                Icon(
                                    imageVector = if (savedToGallery)
                                        Icons.Default.CheckCircle
                                    else Icons.Default.SaveAlt,
                                    contentDescription = stringResource(R.string.save_to_gallery),
                                    tint = if (savedToGallery) Color.Green else Color.White
                                )
                            }

                            Text(
                                text = if (savedToGallery)
                                    stringResource(R.string.saved)
                                else stringResource(R.string.save_to_gallery),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (savedToGallery) Color.Green else Color.White
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = stringResource(R.string.delete),
                                tint = Color.White
                            )
                        }

                        Text(
                            text = stringResource(R.string.delete),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = { isEditingDescription = true },
                            modifier = Modifier.background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_description),
                                tint = Color.White
                            )
                        }

                        Text(
                            text = stringResource(R.string.caption),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(text = stringResource(R.string.delete_photo)) },
                text = { Text(text = stringResource(R.string.are_you_sure_you_want_to_delete_this_photo)) },
                confirmButton = {
                    TextButton(onClick = {
                        val currentIndex = dialogPagerState.currentPage

                        if (photos.size <= 1) {
                            selectedPhotoIndex = null
                        }
                        else {
                            val targetIndex = if (currentIndex >= photos.size - 1) currentIndex - 1 else currentIndex
                            selectedPhotoIndex = targetIndex
                        }

                        photoViewModel.deletePhoto(currentPhoto)
                        showDeleteDialog = false
                        selectedPhotoIndex = null
                    }) { Text(text = stringResource(R.string.yes)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(text = stringResource(R.string.no))
                    }
                }
            )
        }

        if (isEditingDescription) {
            AlertDialog(
                onDismissRequest = { isEditingDescription = false },
                title = { Text(text = stringResource(R.string.edit_caption)) },
                text = {
                    OutlinedTextField(
                        value = descriptionDraft,
                        onValueChange = { descriptionDraft = it },
                        placeholder = { Text(text = stringResource(R.string.describe_this_moment)) },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        photoViewModel.updatePhoto(currentPhoto.copy(description = descriptionDraft))
                        isEditingDescription = false
                    }) { Text(text = stringResource(R.string.save)) }
                },
                dismissButton = {
                    TextButton(onClick = { isEditingDescription = false }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}