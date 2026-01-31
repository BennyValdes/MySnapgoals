package com.mysnapgoals.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mysnapgoals.app.ui.components.Button3D
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme

@Composable
fun AddLine(
    onAddGoal: () -> Unit,
    onAddTodo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button3D(
            onClick = onAddGoal,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            height = 52.dp
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Agregar objetivo"
            )
            Text(
                text = "Agregar Objetivo",
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
        }

        Button3D(
            onClick = onAddTodo,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            height = 52.dp
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Agregar ToDo"
            )
            Text(
                text = "Agregar ToDo",
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewAddLine() {
    SnapGoalsTheme {
        AddLine(
            onAddGoal = {},
            onAddTodo = {}
        )
    }
}
