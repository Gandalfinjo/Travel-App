package com.example.travelapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.travelapp.R
import com.example.travelapp.database.models.Photo
import com.example.travelapp.database.models.enums.TripStatus
import com.example.travelapp.ui.elements.LocationPickerDialog
import com.example.travelapp.ui.elements.PhotoViewerDialog
import com.example.travelapp.ui.viewmodels.PhotoViewModel
import com.example.travelapp.ui.viewmodels.TripViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File

/**
 * Photo album screen for viewing and adding trip photos.
 */
@SuppressLint("LocalContextResourcesRead")
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

    var locationPickerPhoto by remember { mutableStateOf<Photo?>(null) }

    val tripGeoPoint by photoViewModel.tripGeoPoint.collectAsState()
    val isGeocoding by photoViewModel.isGeocoding.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

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

            val latLong = context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream)
            }?.latLong

            photoViewModel.addPhoto(
                Photo(
                    filePath = it.toString(),
                    tripId = tripId,
                    dateTaken = dateTaken,
                    latitude = latLong?.get(0),
                    longitude = latLong?.get(1)
                )
            )
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
                    LocationServices.getFusedLocationProviderClient(context)
                        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            photoViewModel.addPhoto(
                                Photo(
                                    filePath = uri.toString(),
                                    tripId = tripId,
                                    dateTaken = System.currentTimeMillis(),
                                    latitude = location?.latitude,
                                    longitude = location?.longitude
                                )
                            )
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
            val canAddPhotos =
                trip!!.status == TripStatus.ONGOING || trip!!.status == TripStatus.FINISHED

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
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
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
                                    AsyncImage(
                                        model = photos[page].filePath.toUri(),
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
                                                    else
                                                        MaterialTheme.colorScheme.outlineVariant,
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
        PhotoViewerDialog(
            photos = photos,
            initialPage = selectedPhotoIndex!!,
            context = context,
            photoViewModel = photoViewModel,
            onDismiss = { selectedPhotoIndex = null },
            onRequestLocationPicker = { photo ->
                locationPickerPhoto = photo
            }
        )
    }

    if (locationPickerPhoto != null) {
        LaunchedEffect(trip?.location) {
            photoViewModel.resolveTripLocation(trip?.location)
        }

        LocationPickerDialog(
            photo = locationPickerPhoto!!,
            tripGeoPoint = tripGeoPoint,
            resolvedGeoPoint = tripGeoPoint,
            isGeocoding = isGeocoding,
            context = context,
            onDismiss = {
                locationPickerPhoto = null
            },
            onResolveLocation = { name ->
                photoViewModel.resolveTripLocation(name)
            },
            onLocationConfirmed = { updatedPhoto ->
                photoViewModel.updatePhoto(updatedPhoto)
                locationPickerPhoto = null
            }
        )
    }
}