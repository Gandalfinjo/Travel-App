package com.example.travelapp.ui.elements

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import com.example.travelapp.R
import com.example.travelapp.database.models.Photo
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Location picker dialog — tabbed: "Type" | "Map"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    photo: Photo,
    tripGeoPoint: GeoPoint?,
    resolvedGeoPoint: GeoPoint?,
    isGeocoding: Boolean,
    context: Context,
    onDismiss: () -> Unit,
    onResolveLocation: (String) -> Unit,
    onLocationConfirmed: (Photo) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var locationText by remember { mutableStateOf("") }

    var pickedLocation by remember {
        mutableStateOf(
            if (photo.latitude != null && photo.longitude != null)
                GeoPoint(photo.latitude, photo.longitude)
            else null
        )
    }

    var locationPinChanged by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab, locationText) {
        if (selectedTab == 1) {
            val nameToResolve = locationText.trim().ifBlank {
                if (photo.latitude != null && photo.longitude != null) null
                else photo.locationName
            }

            if (nameToResolve != null) {
                pickedLocation = null
                onResolveLocation(nameToResolve)
            }
            else if (photo.latitude != null && photo.longitude != null) {
                pickedLocation = GeoPoint(photo.latitude, photo.longitude)
            }
        }
    }

    LaunchedEffect(resolvedGeoPoint) {
        if (selectedTab == 1 && resolvedGeoPoint != null) {
            pickedLocation = resolvedGeoPoint
        }
    }

    val markerDrawable = remember {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_push_pin)
        bitmap.scale(96, 96, false).toDrawable(context.resources)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (photo.latitude != null || !photo.locationName.isNullOrBlank())
                                    stringResource(R.string.edit_location)
                                else
                                    stringResource(R.string.add_location),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.close)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text(text = stringResource(R.string.type_location)) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )

                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(text = stringResource(R.string.pick_on_map)) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
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
                                        text = stringResource(R.string.enter_location),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    OutlinedTextField(
                                        value = locationText,
                                        onValueChange = { locationText = it },
                                        placeholder = {
                                            Text(stringResource(R.string.location))
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions = KeyboardOptions(
                                            capitalization = KeyboardCapitalization.Words
                                        ),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onLocationConfirmed(
                                        photo.copy(
                                            latitude = null,
                                            longitude = null,
                                            locationName = locationText.trim()
                                        )
                                    )
                                },
                                enabled = locationText.isNotBlank(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = stringResource(R.string.confirm_location))
                            }
                        }
                    }

                    1 -> {
                        if (isGeocoding) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        else {
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
                                        val center = pickedLocation ?: resolvedGeoPoint ?: tripGeoPoint
                                        center?.let { controller.setCenter(it) }
                                    }
                                },
                                update = { mapView ->
                                    mapView.overlays.clear()

                                    pickedLocation?.let { mapView.controller.animateTo(it) }

                                    mapView.overlays.add(
                                        object : org.osmdroid.views.overlay.Overlay() {
                                            override fun onSingleTapConfirmed(
                                                e: android.view.MotionEvent,
                                                mapView: MapView
                                            ): Boolean {
                                                pickedLocation = mapView.projection.fromPixels(
                                                    e.x.toInt(), e.y.toInt()
                                                ) as GeoPoint
                                                locationPinChanged = true
                                                return true
                                            }
                                        }
                                    )

                                    pickedLocation?.let { loc ->
                                        val marker = Marker(mapView).apply {
                                            position = loc
                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                            icon = markerDrawable
                                        }

                                        mapView.overlays.add(marker)
                                    }

                                    mapView.invalidate()
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            if (pickedLocation == null) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                    tonalElevation = 2.dp
                                ) {
                                    Text(
                                        text = stringResource(R.string.tap_map_to_set_location),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    pickedLocation?.let { loc ->
                                        onLocationConfirmed(
                                            photo.copy(
                                                latitude = loc.latitude,
                                                longitude = loc.longitude,
                                                locationName = null
                                            )
                                        )
                                    }
                                },
                                enabled = pickedLocation != null && locationPinChanged,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = stringResource(R.string.confirm_location))
                            }
                        }
                    }
                }
            }
        }
    }
}