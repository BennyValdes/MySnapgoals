package com.mysnapgoals.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.res.stringResource
import com.mysnapgoals.app.R
import com.mysnapgoals.app.ui.components.Panel3D
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme
import kotlinx.coroutines.delay

@Composable
fun PercentageLine(
    dayCompleted: Int,
    dayPending: Int,
    dayOverdue: Int,
    dayCompletedCount: Int,
    dayPendingCount: Int,
    dayOverdueCount: Int,
    weekCompleted: Int,
    weekPending: Int,
    weekOverdue: Int,
    weekCompletedCount: Int,
    weekPendingCount: Int,
    weekOverdueCount: Int,
    monthCompleted: Int,
    monthPending: Int,
    monthOverdue: Int,
    monthCompletedCount: Int,
    monthPendingCount: Int,
    monthOverdueCount: Int,
    yearCompleted: Int,
    yearPending: Int,
    yearOverdue: Int,
    yearCompletedCount: Int,
    yearPendingCount: Int,
    yearOverdueCount: Int,
    modifier: Modifier = Modifier
) {
    Panel3D(
        modifier = modifier.fillMaxWidth(),
        depth = 6.dp,
        elevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BarCell(
                completed = dayCompleted,
                pending = dayPending,
                overdue = dayOverdue,
                completedCount = dayCompletedCount,
                pendingCount = dayPendingCount,
                overdueCount = dayOverdueCount,
                label = stringResource(R.string.percentage_day)
            )

            BarCell(
                completed = weekCompleted,
                pending = weekPending,
                overdue = weekOverdue,
                completedCount = weekCompletedCount,
                pendingCount = weekPendingCount,
                overdueCount = weekOverdueCount,
                label = stringResource(R.string.percentage_week)
            )

            BarCell(
                completed = monthCompleted,
                pending = monthPending,
                overdue = monthOverdue,
                completedCount = monthCompletedCount,
                pendingCount = monthPendingCount,
                overdueCount = monthOverdueCount,
                label = stringResource(R.string.percentage_month)
            )

            BarCell(
                completed = yearCompleted,
                pending = yearPending,
                overdue = yearOverdue,
                completedCount = yearCompletedCount,
                pendingCount = yearPendingCount,
                overdueCount = yearOverdueCount,
                label = stringResource(R.string.percentage_year)
            )
        }
    }
}

@Composable
private fun BarCell(
    completed: Int,
    pending: Int,
    overdue: Int,
    completedCount: Int,
    pendingCount: Int,
    overdueCount: Int,
    label: String
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
    val completedColor = MaterialTheme.colorScheme.secondary
    val pendingColor = MaterialTheme.colorScheme.primary
    val overdueColor = MaterialTheme.colorScheme.error
    val textColor = MaterialTheme.colorScheme.onSurface
    val barHeight = 64.dp
    val barWidth = 14.dp

    val c = completed.coerceIn(0, 100)
    val p = pending.coerceIn(0, 100)
    val o = overdue.coerceIn(0, 100)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BarWithTooltip(
            completed = c,
            pending = p,
            overdue = o,
            completedCount = completedCount,
            pendingCount = pendingCount,
            overdueCount = overdueCount,
            barHeight = barHeight,
            barWidth = barWidth,
            trackColor = trackColor,
            completedColor = completedColor,
            pendingColor = pendingColor,
            overdueColor = overdueColor
        )

        Text(
            text = "${c}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
private fun BarWithTooltip(
    completed: Int,
    pending: Int,
    overdue: Int,
    completedCount: Int,
    pendingCount: Int,
    overdueCount: Int,
    barHeight: androidx.compose.ui.unit.Dp,
    barWidth: androidx.compose.ui.unit.Dp,
    trackColor: androidx.compose.ui.graphics.Color,
    completedColor: androidx.compose.ui.graphics.Color,
    pendingColor: androidx.compose.ui.graphics.Color,
    overdueColor: androidx.compose.ui.graphics.Color
) {
    var showTooltip by remember { mutableStateOf(false) }
    val tooltipText = stringResource(
        R.string.percentage_tooltip,
        completedCount,
        completed,
        pendingCount,
        pending,
        overdueCount,
        overdue
    )

    LaunchedEffect(showTooltip) {
        if (showTooltip) {
            delay(1500)
            showTooltip = false
        }
    }

    Column(
        modifier = Modifier
            .height(barHeight)
            .width(barWidth)
            .clip(RoundedCornerShape(8.dp))
            .background(trackColor)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { showTooltip = true })
            },
        verticalArrangement = Arrangement.Bottom
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight * (completed / 100f))
                .clip(RoundedCornerShape(8.dp))
                .background(completedColor)
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight * (pending / 100f))
                .clip(RoundedCornerShape(8.dp))
                .background(pendingColor)
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight * (overdue / 100f))
                .clip(RoundedCornerShape(8.dp))
                .background(overdueColor)
        )
    }

    if (showTooltip) {
        Popup(alignment = Alignment.TopCenter, offset = IntOffset(0, -12)) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = tooltipText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PercentageLinePreview() {
    SnapGoalsTheme {
        PercentageLine(
            dayCompleted = 40,
            dayPending = 30,
            dayOverdue = 30,
            dayCompletedCount = 2,
            dayPendingCount = 1,
            dayOverdueCount = 1,
            weekCompleted = 20,
            weekPending = 50,
            weekOverdue = 30,
            weekCompletedCount = 3,
            weekPendingCount = 6,
            weekOverdueCount = 2,
            monthCompleted = 10,
            monthPending = 40,
            monthOverdue = 50,
            monthCompletedCount = 4,
            monthPendingCount = 12,
            monthOverdueCount = 8,
            yearCompleted = 70,
            yearPending = 20,
            yearOverdue = 10,
            yearCompletedCount = 120,
            yearPendingCount = 30,
            yearOverdueCount = 10
        )
    }
}
