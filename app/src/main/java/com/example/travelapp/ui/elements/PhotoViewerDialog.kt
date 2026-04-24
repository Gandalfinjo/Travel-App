package com.example.travelapp.ui.elements

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import com.example.travelapp.R
import com.example.travelapp.database.models.Photo
import com.example.travelapp.ui.viewmodels.PhotoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full-screen photo viewer dialog
 */
@Composable
fun PhotoViewerDialog(
    photos: List<Photo>,
    initialPage: Int,
    context: Context,
    photoViewModel: PhotoViewModel,
    onDismiss: () -> Unit,
    onRequestLocationPicker: (Photo) -> Unit
) {
    val dialogPagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { photos.size }
    )

    val currentPhoto = photos[dialogPagerState.currentPage]
    val isCameraPhoto = currentPhoto.filePath.startsWith("content://${context.packageName}.provider")
    val hasLocation = currentPhoto.latitude != null && currentPhoto.longitude != null || !currentPhoto.locationName.isNullOrBlank()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditingDescription by remember { mutableStateOf(false) }
    var descriptionDraft by remember(dialogPagerState.currentPage) {
        mutableStateOf(currentPhoto.description ?: "")
    }

    val savedPhotoIds by photoViewModel.savedPhotoIds.collectAsState()
    val savedToGallery = savedPhotoIds.contains(currentPhoto.id)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
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
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
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

                    IconButton(
                        onClick = {
                            onDismiss()
                            onRequestLocationPicker(currentPhoto)
                        },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (hasLocation) Icons.Default.LocationOn else Icons.Default.LocationOff,
                            contentDescription = if (hasLocation)
                                stringResource(R.string.location_available)
                            else
                                stringResource(R.string.add_location),
                            tint = if (hasLocation)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                val formattedDate = remember(currentPhoto.dateTaken) {
                    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        .format(Date(currentPhoto.dateTaken))
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
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
                    PhotoActionButton(
                        icon = if (savedToGallery) Icons.Default.CheckCircle else Icons.Default.SaveAlt,
                        label = if (savedToGallery)
                            stringResource(R.string.saved)
                        else
                            stringResource(R.string.save_to_gallery),
                        tint = if (savedToGallery) Color.Green else Color.White,
                        backgroundAlpha = if (savedToGallery) 0.1f else 0.2f,
                        onClick = {
                            if (!savedToGallery) {
                                photoViewModel.savePhotoToGallery(
                                    contentResolver = context.contentResolver,
                                    photo = currentPhoto
                                )
                            }
                        }
                    )
                }

                PhotoActionButton(
                    icon = Icons.Default.DeleteOutline,
                    label = stringResource(R.string.delete),
                    onClick = { showDeleteDialog = true }
                )

                PhotoActionButton(
                    icon = Icons.Default.Edit,
                    label = stringResource(R.string.caption),
                    onClick = { isEditingDescription = true }
                )
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
                    photoViewModel.deletePhoto(currentPhoto)
                    showDeleteDialog = false
                    onDismiss()
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