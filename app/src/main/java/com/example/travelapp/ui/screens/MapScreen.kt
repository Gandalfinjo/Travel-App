package com.example.travelapp.ui.screens

import android.Manifest
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.travelapp.api.maps.OTMPoi
import com.example.travelapp.api.maps.fetchNearbyPOI
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }

    LaunchedEffect(locationPermission.status.isGranted) {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )

        if (locationPermission.status.isGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = GeoPoint(it.latitude, it.longitude)
                    }
                }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (locationPermission.status.isGranted) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                    }
                },
                update = { mapView ->
                    val centerPoint = currentLocation ?: GeoPoint(44.8176, 20.4569)
                    mapView.controller.setCenter(centerPoint)

                    val museumMarker = Marker(mapView)
                    museumMarker.position = GeoPoint(44.8176, 20.4569)
                    museumMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    museumMarker.title = "Muzej primer"
                    mapView.overlays.add(museumMarker)

                    currentLocation?.let { loc ->
                        val userMarker = Marker(mapView)
                        userMarker.position = loc
                        userMarker.title = "Ti si ovde"
                        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(userMarker)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Button(
                onClick = { locationPermission.launchPermissionRequest() },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
            ) {
                Text("Dozvoli lokaciju")
            }
        }
    }
}