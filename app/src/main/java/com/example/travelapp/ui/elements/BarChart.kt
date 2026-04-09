package com.example.travelapp.ui.elements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.travelapp.R
import com.example.travelapp.ui.viewmodels.TripWithExpenses

/**
 * Composable that displays a bar chart of trip budgets.
 *
 * Each bar represents a single [TripWithExpenses], scaled relative to the maximum budget.
 * Includes truncated labels and a legend with full trip details.
 *
 * @param trips List of trips to visualize
 * @param modifier Modifier for styling and layout
 */
@Composable
fun BarChart(
    trips: List<TripWithExpenses>,
    modifier: Modifier = Modifier,
    currency: String,
    onTripClick: (Int) -> Unit = {}
) {
    if (trips.isEmpty()) {
        Text(
            text = stringResource(R.string.no_data),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val maxTotal = trips.maxOfOrNull { it.totalSpent } ?: 1.0

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        trips.forEach { tripWithExpenses ->
            val fraction = (tripWithExpenses.totalSpent / maxTotal).toFloat()

            Surface(
                onClick = { onTripClick(tripWithExpenses.trip.id) },
                shape = RoundedCornerShape(8.dp),
                color = Color.Transparent
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tripWithExpenses.trip.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Row {
                            Text(
                                text = "${"%.2f".format(tripWithExpenses.totalSpent)} $currency",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF185FA5),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

        }
    }
}