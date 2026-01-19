package com.mysnapgoals.app.ui.components

import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
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
    val target: Int? = null
)

@Composable
fun TodayItem(
    model: TodayItemUiModel,
    onToggleDone: (String) -> Unit,
    onIncrement: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val onContainer = MaterialTheme.colorScheme.onSurface
    val isGoal = model.type == TodayItemType.GOAL

    val progressText =
        if (isGoal && model.current != null && model.target != null) {
            "${model.current}/${model.target}"
        } else null

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    role = Role.Button,
                    onClick = { onToggleDone(model.id) }
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (model.isDone) Icons.Filled.Check else Icons.Filled.Clear,
                contentDescription = if (model.isDone) "Completado" else "Pendiente",
                tint = if (model.isDone) onContainer.copy(alpha = 0.55f)
                else onContainer.copy(alpha = 0.55f),
                modifier = Modifier.size(22.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
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

                if (progressText != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onContainer.copy(alpha = 0.70f)
                    )
                }
            }

            if (isGoal && !model.isDone) {
                IconButton(
                    onClick = {
                        Log.d("SnapGoals", "Special onClick: id=${model.id}")
                        onIncrement(model.id)
                              },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Incrementar",
                        tint = onContainer
                    )
                }
            }
        }
    }
}