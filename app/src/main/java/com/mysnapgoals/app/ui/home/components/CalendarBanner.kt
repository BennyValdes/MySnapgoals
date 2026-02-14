package com.mysnapgoals.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.mysnapgoals.app.R
import com.mysnapgoals.app.ui.components.Button3D
import com.mysnapgoals.app.ui.components.Panel3D
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme

@Composable
fun CalendarBanner(
    timeText: String,
    dayOfWeekText: String,
    dateText: String,
    modifier: Modifier = Modifier,
    onPomodoroClick: () -> Unit = {}
) {
    Panel3D(
        modifier = modifier
            .fillMaxWidth(),
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
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Button3D(
                    text = stringResource(R.string.calendar_pomodoro),
                    onClick = onPomodoroClick,
                    height = 38.dp,
                    depth = 3.dp
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                    Text(
                        text = dayOfWeekText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewCalendarBanner() {
    SnapGoalsTheme {
        CalendarBanner(
            timeText= "09:42",
            dayOfWeekText= "Lunes",
            dateText= "2025/12/31"
        )
    }
}
