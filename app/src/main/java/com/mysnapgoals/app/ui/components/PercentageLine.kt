package com.mysnapgoals.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme

@Composable
fun PercentageLine(
    dayPercent: Int,
    weekPercent: Int,
    monthPercent: Int,
    yearPercent: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PercentCell(
                percentText = "${dayPercent.coerceIn(0, 100)}%",
                label = "Día"
            )

            PercentCell(
                percentText = "${weekPercent.coerceIn(0, 100)}%",
                label = "Sem"
            )

            PercentCell(
                percentText = "${monthPercent.coerceIn(0, 100)}%",
                label = "Mes"
            )

            PercentCell(
                percentText = "${yearPercent.coerceIn(0, 100)}%",
                label = "Año"
            )
        }
    }
}

@Composable
private fun PercentCell(
    percentText: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = percentText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
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