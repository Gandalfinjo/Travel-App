package com.example.travelapp.ui.elements

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import coil.compose.AsyncImage

@Composable
fun ZoomableImage(
    model: Any,
    modifier: Modifier = Modifier,
    onScaleChanged: (Float) -> Unit = {}
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(Size.Zero) }
    var imageSize by remember { mutableStateOf(Size.Zero) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)

        if (scale == 1f) {
            offset = Offset.Zero
        }
        else {
            val renderedWidth: Float
            val renderedHeight: Float

            if (imageSize.width == 0f || imageSize.height == 0f) {
                renderedWidth = size.width
                renderedHeight = size.height
            }
            else {
                val imageAspect = imageSize.width / imageSize.height
                val containerAspect = size.width / size.height

                if (imageAspect > containerAspect) {
                    renderedWidth = size.width
                    renderedHeight = size.width / imageAspect
                }
                else {
                    renderedHeight = size.height
                    renderedWidth = size.height * imageAspect
                }
            }

            val maxX = (renderedWidth * (scale - 1f)) / 2f
            val maxY = (renderedHeight * (scale - 1f)) / 2f

            offset = Offset(
                x = (offset.x + offsetChange.x * scale).coerceIn(-maxX, maxX),
                y = (offset.y + offsetChange.y * scale).coerceIn(-maxY, maxY)
            )
        }

        onScaleChanged(scale)
    }

    LaunchedEffect(scale) {
        if (scale == 1f) offset = Offset.Zero
    }

    AsyncImage(
        model = model,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        onSuccess = { state ->
            val painter = state.painter
            imageSize = Size(
                painter.intrinsicSize.width,
                painter.intrinsicSize.height
            )
        },
        modifier = modifier
            .onSizeChanged { size = Size(it.width.toFloat(), it.height.toFloat()) }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(
                state = transformableState,
                canPan = { scale > 1f }
            )
    )
}