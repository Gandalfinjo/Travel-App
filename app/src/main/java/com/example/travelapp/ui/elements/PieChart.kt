package com.example.travelapp.ui.elements

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.travelapp.R
import com.example.travelapp.database.models.enums.TripStatus

/**
 * Composable that displays a pie chart of trips grouped by [TripStatus].
 *
 * Each slice represents the proportion of trips for a given status.
 * Also displays a legend with counts per status.
 *
 * @param data Map of [TripStatus] to number of trips
 */
@Composable
fun PieChart(data: Map<TripStatus, Int>) {
    if (data.isEmpty()) {
        Text(
            text = stringResource(R.string.no_data),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val total = data.values.sum().toFloat()
    val statusColors = mapOf(
        TripStatus.FINISHED  to Color(0xFF6B4E00),
        TripStatus.PLANNED   to Color(0xFF185FA5),
        TripStatus.ONGOING   to Color(0xFF0F6E56),
        TripStatus.CANCELLED to Color(0xFF9B1C1C)
    )
    val entries = data.entries.toList()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .size(110.dp)
                .aspectRatio(1f)
        ) {
            var startAngle = -90f
            val radius = size.minDimension / 2f
            val strokeWidth = radius * 0.4f
            val adjustedRadius = radius - strokeWidth / 2f

            entries.forEach { (status, count) ->
                val sweepAngle = (count / total) * 360f

                drawArc(
                    color = statusColors[status] ?: Color.Gray,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle - 2f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                    size = Size(adjustedRadius * 2, adjustedRadius * 2),
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                )

                startAngle += sweepAngle
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            entries.forEach { (status, count) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(statusColors[status] ?: Color.Gray)
                        )

                        Text(
                            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}