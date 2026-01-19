package com.mysnapgoals.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    initialFilterType: TaskFilterType,
    initialSort: TaskSort,
    onApply: (TaskFilterType, TaskSort) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var filterType by remember(initialFilterType) { mutableStateOf(initialFilterType) }
    var sort by remember(initialSort) { mutableStateOf(initialSort) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun closeSheet() {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = { closeSheet() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Filtros", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(12.dp))
            Text("Tipo", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))

            RadioRow("Todos", filterType == TaskFilterType.ALL) { filterType = TaskFilterType.ALL }
            RadioRow("ToDos", filterType == TaskFilterType.TODO) { filterType = TaskFilterType.TODO }
            RadioRow("Objetivos", filterType == TaskFilterType.GOAL) { filterType = TaskFilterType.GOAL }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            Text("Orden", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))

            RadioRow("Recientes", sort == TaskSort.RECENT) { sort = TaskSort.RECENT }
            RadioRow("A-Z", sort == TaskSort.ALPHA) { sort = TaskSort.ALPHA }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        onClear()
                        closeSheet()
                    }
                ) { Text("Limpiar") }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        onApply(filterType, sort)
                        closeSheet()
                    }
                ) { Text("Aplicar") }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        RadioButton(selected = selected, onClick = onClick)
    }
}