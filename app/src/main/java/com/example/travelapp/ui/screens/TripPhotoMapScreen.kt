package com.example.travelapp.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.Coil
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.travelapp.R
import com.example.travelapp.database.models.Photo
import com.example.travelapp.ui.elements.ZoomableImage
import com.example.travelapp.ui.viewmodels.PhotoViewModel
import com.example.travelapp.ui.viewmodels.TripViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("LocalContextResourcesRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPhotoMapScreen(
    tripId: Int,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    photoViewModel: PhotoViewModel = hiltViewModel(),
    tripViewModel: TripViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val photos by photoViewModel.getTripPhotos(tripId).collectAsState(initial = emptyList())
    val trip by tripViewModel.getTrip(tripId).collectAsState(initial = null)

    val resolvedPhotos by photoViewModel.resolvedPhotos.collectAsState()

    LaunchedEffect(photos) {
        photoViewModel.resolvePhotoLocations(photos)
    }

    val mappablePhotos = remember(resolvedPhotos) {
        resolvedPhotos.filter { it.latitude != null && it.longitude != null }
    }

    val markerDrawable = remember {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_push_pin)
        bitmap.scale(96, 96, false).toDrawable(context.resources)
    }

    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.photo_map),
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
        if (mappablePhotos.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
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
                            text = stringResource(R.string.no_photos_on_map),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.add_locations_to_photos_to_see_them_here),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        else {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            ) {
                val currentInfoWindow = remember { mutableStateOf<InfoWindow?>(null) }

                AndroidView(
                    factory = { ctx ->
                        Configuration.getInstance().load(
                            ctx,
                            ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
                        )
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(12.0)
                            // Center on the first mappable photo
                            mappablePhotos.first().let {
                                controller.setCenter(GeoPoint(it.latitude!!, it.longitude!!))
                            }
                        }
                    },
                    update = { mapView ->
                        mapView.overlays.clear()

                        mapView.overlays.add(object : org.osmdroid.views.overlay.Overlay() {
                            override fun onSingleTapConfirmed(
                                e: android.view.MotionEvent,
                                mapView: MapView
                            ): Boolean {
                                mapView.overlays
                                    .filterIsInstance<Marker>()
                                    .forEach { it.closeInfoWindow() }
                                return false
                            }
                        })

                        mappablePhotos.forEach { photo ->
                            val marker = Marker(mapView).apply {
                                position = GeoPoint(photo.latitude!!, photo.longitude!!)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                icon = markerDrawable
                                infoWindow = PhotoInfoWindow(
                                    mapView = mapView,
                                    photo = photo,
                                    onPhotoClick = { selectedPhoto = it }
                                )

                                setOnMarkerClickListener { marker, _ ->
                                    currentInfoWindow.value?.close()
                                    marker.showInfoWindow()
                                    currentInfoWindow.value = marker.infoWindow
                                    true
                                }
                            }
                            mapView.overlays.add(marker)
                        }

                        mapView.invalidate()
                    },
                    modifier = Modifier.fillMaxSize(),
                    onRelease = { mapView ->
                        mapView.onDetach()
                    }
                )
            }
        }
    }

    if (selectedPhoto != null) {
        Dialog(
            onDismissRequest = { selectedPhoto = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                ZoomableImage(
                    model = selectedPhoto!!.filePath.toUri(),
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = { selectedPhoto = null },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

class PhotoInfoWindow(
    mapView: MapView,
    private val photo: Photo,
    private val onPhotoClick: (Photo) -> Unit
) : InfoWindow(FrameLayout(mapView.context).also {
    it.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
}, mapView) {

    override fun onOpen(item: Any?) {
        val container = mView as FrameLayout
        container.removeAllViews()

        val composeView = object : AbstractComposeView(container.context) {
            @Composable
            override fun Content() {
                PhotoInfoWindowContent(
                    photo = photo,
                    onClick = {
                        onPhotoClick(photo)
                        close()
                    }
                )
            }
        }

        container.addView(composeView)
    }

    override fun onClose() {
        (mView as FrameLayout).removeAllViews()
    }
}

@Composable
private fun PhotoInfoWindowContent(
    photo: Photo,
    onClick: () -> Unit
) {
    val formattedDate = remember(photo.dateTaken) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(photo.dateTaken))
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.width(180.dp)
    ) {
        AsyncImage(
            model = photo.filePath.toUri(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
        )

        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )

            if (!photo.locationName.isNullOrBlank()) {
                Text(
                    text = photo.locationName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}