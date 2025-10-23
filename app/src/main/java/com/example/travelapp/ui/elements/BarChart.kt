package com.example.travelapp.ui.elements

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.travelapp.database.models.Trip

@Composable
fun BarChart(
    trips: List<Trip>,
    modifier: Modifier = Modifier
) {
    if (trips.isEmpty()) {
        Text("No data available")
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val barColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = canvasWidth / (trips.size * 1.5f)
            val spacing = barWidth * 0.5f
            val maxBudget = trips.maxOfOrNull { it.budget } ?: 1.0

            trips.forEachIndexed { index, trip ->
                val barHeight = (trip.budget / maxBudget * (canvasHeight * 0.7f)).toFloat()
                val x = index * (barWidth + spacing) + spacing
                val y = canvasHeight - barHeight - 40f

                // Draw bar
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Draw trip name (truncated)
                val tripName = if (trip.name.length > 10)
                    trip.name.take(8) + ".."
                else
                    trip.name

                val textLayout = textMeasurer.measure(tripName)
                val textWidth = textLayout.size.width
                val textHeight = textLayout.size.height

                drawText(
                    textMeasurer = textMeasurer,
                    text = tripName,
                    topLeft = Offset(
                        x + (barWidth - textWidth) / 2,
                        canvasHeight - textHeight + 2f
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Legend
        trips.forEach { trip ->
            Text(
                text = "${trip.name}: ${trip.budget} ${trip.currency}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}