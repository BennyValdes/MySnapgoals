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
import com.mysnapgoals.app.ui.home.components.TodayItem
import com.mysnapgoals.app.ui.home.components.TodayItemType
import com.mysnapgoals.app.ui.home.components.TodayItemUiModel
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme

@Composable
fun TodayLine(
    items: List<TodayItemUiModel>,
    onToggleDone: (String) -> Unit,
    onIncrementGoal: (String) -> Unit,
    onDecrementGoal: (String) -> Unit,
    onUncomplete: (String) -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxItems: Int = 5
) {
    val ordered =
        items
            .sortedWith(compareBy { it.type != TodayItemType.TODO })
            .take(maxItems)

    val total = items.size
    val pending = items.count { !it.isDone }

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
                text = "Hoy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            // Contador simple: pendientes/total
            if (total > 0) {
                Text(
                    text = "$pending/$total",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (ordered.isEmpty()) {
            Text(
                text = "No tienes nada pendiente hoy.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            ordered.forEach { item ->
                TodayItem(
                    model = item,
                    onToggleDone = onToggleDone,
                    onIncrement = onIncrementGoal,
                    onDecrement = onDecrementGoal,
                    onUncomplete = onUncomplete,
                    onItemClick = onItemClick
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun TodayLinePreviewToDo() {
    SnapGoalsTheme {
        TodayLine(
            items = listOf(TodayItemUiModel(
                id = "1234",
                type = TodayItemType.TODO,
                title = "GoToGym",
                isDone = false,
            )),
            onToggleDone = {},
            onIncrementGoal = {},
            onDecrementGoal = {},
            onUncomplete = {},
            onItemClick = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
fun TodayLinePreviewGoal() {
    SnapGoalsTheme {
        TodayLine(
            items = listOf(
                TodayItemUiModel(
                    id = "1234",
                    type = TodayItemType.GOAL,
                    title = "GoToGym",
                    isDone = false,
                    current = 3,
                    target = 10,
                )
            ),
            onToggleDone = {},
            onIncrementGoal = {},
            onDecrementGoal = {},
            onUncomplete = {},
            onItemClick = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
fun TodayLinePreviewEmpty() {
    SnapGoalsTheme {
        TodayLine(
            items = listOf(),
            onToggleDone = {},
            onIncrementGoal = {},
            onDecrementGoal = {},
            onUncomplete = {},
            onItemClick = {},
        )
    }
}
