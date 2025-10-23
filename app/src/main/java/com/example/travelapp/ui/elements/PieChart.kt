package com.example.travelapp.ui.elements

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.travelapp.database.models.enums.TripStatus
import kotlin.math.min

@Composable
fun PieChart(data: Map<TripStatus, Int>) {
    if (data.isEmpty()) {
        Text("No data available", textAlign = TextAlign.Center)
        return
    }

    val total = data.values.sum().toFloat()
    val colors = listOf(
        Color(0xFFEF5350), // CANCELLED
        Color(0xFFFFA726), // PLANNED
        Color(0xFF42A5F5), // FINISHED
        Color(0xFF66BB6A) // ONGOING
    )
    val entries = data.entries.toList()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .height(200.dp)
                .aspectRatio(1f)
        ) {
            var startAngle = 0f
            val size = min(size.width, size.height)
            val radius = size / 2f

            entries.forEachIndexed { index, entry ->
                val sweepAngle = (entry.value / total) * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    size = Size(radius * 2, radius * 2)
                )
                startAngle += sweepAngle
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        entries.forEachIndexed { index, entry ->
            Text(
                text = "${entry.key.name}: ${entry.value}",
                color = colors[index % colors.size]
            )
        }
    }
}