package com.mysnapgoals.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mysnapgoals.app.ui.components.TodayItem
import com.mysnapgoals.app.ui.components.TodayItemType
import com.mysnapgoals.app.ui.components.TodayItemUiModel

@Composable
fun TodayLine(
    items: List<TodayItemUiModel>,
    onToggleDone: (String) -> Unit,
    onIncrementGoal: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxItems: Int = 5
) {
    // Orden: TODO primero, luego GOAL
    val ordered =
        items
            .sortedWith(compareBy { it.type != TodayItemType.TODO })
            .take(maxItems)

    val total = items.size
    val pending = items.count { !it.isDone }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hoy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Contador simple: pendientes/total
            if (total > 0) {
                Text(
                    text = "$pending/$total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (ordered.isEmpty()) {
            Text(
                text = "No tienes nada pendiente hoy.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            ordered.forEach { item ->
                TodayItem(
                    model = item,
                    onToggleDone = onToggleDone,
                    onIncrement = onIncrementGoal,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}