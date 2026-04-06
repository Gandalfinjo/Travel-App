package com.example.travelapp.ui.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage

@Composable
fun UserAvatar(
    firstname: String?,
    lastname: String?,
    profilePicturePath: String?,
    size: Dp,
    onClick: (() -> Unit)? = null,
    showEditBadge: Boolean = false
) {
    val initials = buildString {
        firstname?.firstOrNull()?.let { append(it.uppercaseChar()) }
        lastname?.firstOrNull()?.let { append(it.uppercaseChar()) }
    }.ifEmpty { "?" }

    Box(
        modifier = Modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        if (profilePicturePath != null) {
            AsyncImage(
                model = profilePicturePath.toUri(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
            )
        }
        else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = when {
                        size >= 64.dp -> MaterialTheme.typography.headlineSmall
                        size >= 48.dp -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.labelMedium
                    },
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        if (showEditBadge) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(size * 0.15f),
                    tint = Color.White
                )
            }
        }
    }
}