package com.mysnapgoals.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.mysnapgoals.app.R
import androidx.compose.ui.res.stringResource
import com.mysnapgoals.app.domain.model.GoalPeriodicity
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme

enum class TodayItemType {
    TODO,
    GOAL
}

data class TodayItemUiModel(
    val id: String,
    val type: TodayItemType,
    val title: String,
    val isDone: Boolean,
    val current: Int? = null,
    val target: Int? = null,
    val scheduledDay: Long? = null,
    val periodicity: GoalPeriodicity? = null,
    val dueDay: Long? = null
)

@Composable
fun TodayItem(
    model: TodayItemUiModel,
    onToggleDone: (String) -> Unit,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onUncomplete: (String) -> Unit,
    onItemClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val onContainer = MaterialTheme.colorScheme.onSurface
    val isGoal = model.type == TodayItemType.GOAL

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (model.isDone) {
                IconButton(
                    onClick = { onUncomplete(model.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_checkbox_checked),
                        contentDescription = stringResource(R.string.content_desc_completed),
                        tint = onContainer.copy(alpha = 0.70f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = { onToggleDone(model.id) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_checkbox_outline),
                        contentDescription = stringResource(R.string.content_desc_pending),
                        tint = onContainer.copy(alpha = 0.55f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (onItemClick != null) {
                            Modifier.clickable(
                                role = Role.Button,
                                onClick = { onItemClick(model.id) }
                            )
                        } else {
                            Modifier
                        }
                    )
            ) {
                Text(
                    text = model.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(if (model.isDone) 0.55f else 1f)
                )

                if (isGoal && model.current != null && model.target != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${model.current}/${model.target}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onContainer.copy(alpha = 0.70f)
                    )
                }
            }

            if (isGoal && !model.isDone) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onDecrement(model.id) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_remove),
                            contentDescription = stringResource(R.string.content_desc_decrement),
                            tint = onContainer
                        )
                    }

                    IconButton(
                        onClick = { onIncrement(model.id) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = stringResource(R.string.content_desc_increment),
                            tint = onContainer
                        )
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodayItemTodoPreview() {
    SnapGoalsTheme {
        TodayItem(
            model = TodayItemUiModel(
                id = "1",
                type = TodayItemType.TODO,
                title = "Read 10 pages",
                isDone = false
            ),
            onToggleDone = {},
            onIncrement = {},
            onDecrement = {},
            onUncomplete = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TodayItemGoalPreview() {
    SnapGoalsTheme {
        TodayItem(
            model = TodayItemUiModel(
                id = "2",
                type = TodayItemType.GOAL,
                title = "Meditation",
                isDone = false,
                current = 2,
                target = 5
            ),
            onToggleDone = {},
            onIncrement = {},
            onDecrement = {},
            onUncomplete = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
