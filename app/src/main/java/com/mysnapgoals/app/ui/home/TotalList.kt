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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import com.mysnapgoals.app.R
import com.mysnapgoals.app.ui.home.components.TodayItem
import com.mysnapgoals.app.ui.home.components.TodayItemType
import com.mysnapgoals.app.ui.home.components.TodayItemUiModel
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme
import java.time.LocalDate

@Composable
fun TotalList(
    items: List<TodayItemUiModel>,
    onToggleDone: (String) -> Unit,
    onIncrementGoal: (String) -> Unit,
    onDecrementGoal: (String) -> Unit,
    onUncomplete: (String) -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now().toEpochDay()
    val ordered =
        items.sortedWith(
            compareBy<TodayItemUiModel> {
                when (it.type) {
                    TodayItemType.TODO -> it.scheduledDay ?: today
                    TodayItemType.GOAL -> it.dueDay ?: today
                }
            }.thenBy { it.title.lowercase() }
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
                text = stringResource(R.string.home_total_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            Text(
                text = "${items.size}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        if (ordered.isEmpty()) {
            Text(
                text = stringResource(R.string.common_no_items),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
            onDecrementGoal = {},
            onUncomplete = {},
            onItemClick = {}
        )
    }
}
