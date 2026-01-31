package com.mysnapgoals.app.ui.home.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mysnapgoals.app.ui.components.Panel3D
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme

@Composable
fun PercentageLine(
    dayPercent: Int,
    weekPercent: Int,
    monthPercent: Int,
    yearPercent: Int,
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
                percent = dayPercent,
                label = "Día"
            )

            BarCell(
                percent = weekPercent,
                label = "Sem"
            )

            BarCell(
                percent = monthPercent,
                label = "Mes"
            )

            BarCell(
                percent = yearPercent,
                label = "Año"
            )
        }
    }
}

@Composable
private fun BarCell(
    percent: Int,
    label: String
) {
    val clamped = percent.coerceIn(0, 100)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val fillColor = MaterialTheme.colorScheme.primary
    val barHeight = 64.dp
    val barWidth = 14.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Column(
            modifier = Modifier
                .height(barHeight)
                .width(barWidth)
                .clip(RoundedCornerShape(8.dp))
                .background(trackColor),
            verticalArrangement = Arrangement.Bottom
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight * (clamped / 100f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(fillColor)
            )
        }

        Text(
            text = "$clamped%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PercentageLinePreview() {
    SnapGoalsTheme {
        PercentageLine(
            dayPercent = 5,
            weekPercent = 10,
            monthPercent = 40,
            yearPercent = 55,
        )
    }
}
