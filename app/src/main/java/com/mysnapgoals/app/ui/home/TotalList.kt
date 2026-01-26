package com.mysnapgoals.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.mysnapgoals.app.ui.components.TodayItem
import com.mysnapgoals.app.ui.components.TodayItemType
import com.mysnapgoals.app.ui.components.TodayItemUiModel
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme

@Composable
fun TotalList(
    items: List<TodayItemUiModel>,
    onToggleDone: (String) -> Unit,
    onIncrementGoal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ordered =
        items.sortedWith(
            compareBy<TodayItemUiModel> { it.type != TodayItemType.TODO }
                .thenBy { it.title.lowercase() }
        )

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Todo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${items.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                fontWeight = FontWeight.Medium
            )
        }

        if (ordered.isEmpty()) {
            Text(
                text = "No hay elementos para mostrar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
        } else {
            ordered.forEach { item ->
                TodayItem(
                    model = item,
                    onToggleDone = onToggleDone,
                    onIncrement = onIncrementGoal,
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun TotalListPreview() {
    SnapGoalsTheme {
        TotalList(
            items = listOf(
                TodayItemUiModel(
                    id = "1234",
                    type = TodayItemType.TODO,
                    title = "GoToGym",
                    isDone = false,
                ),
                TodayItemUiModel(
                    id = "1234",
                    type = TodayItemType.GOAL,
                    title = "Gym",
                    isDone = false,
                    current = 3,
                    target = 10,
                )
            ),
            onToggleDone = {},
            onIncrementGoal = {},
        )
    }
}