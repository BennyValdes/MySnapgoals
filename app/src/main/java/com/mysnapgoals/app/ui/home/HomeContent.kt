package com.mysnapgoals.app.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mysnapgoals.app.ui.components.Button3D
import com.mysnapgoals.app.ui.components.DropDownMenu

@Composable
fun HomeContent(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .fillMaxSize()
    ) {
        item {
            DropDownMenu(
                label = "Price",
                onClick = {},
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
        item {
            Button3D(
                text = "Confirm",
                onClick = { /* action */ },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            HabitProgressCard(
                title = "Savings",
                subtitle = "Retirement",
                streakCount = 13,
                progressPercent = 50,
                modifier = Modifier.padding(top = 10.dp, bottom = 0.dp)
            )
        }
        item {
            HabitProgressCard(
                title = "Savings",
                subtitle = "Retirement",
                streakCount = 13,
                progressPercent = 50,
                modifier = Modifier.padding(top = 10.dp, bottom = 0.dp)
            )
        }
    }
}
