package com.mysnapgoals.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterLine(
    query: String,
    onQueryChanged: (String) -> Unit,
    onTrailingActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showDropdown = query.isBlank()

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier
            .fillMaxWidth(),
        singleLine = true,
        label = { Text("Buscar") },
        trailingIcon = {
            IconButton(onClick = onTrailingActionClick) {
                Icon(
                    imageVector = if (showDropdown) Icons.Filled.ArrowDropDown else Icons.Filled.Search,
                    contentDescription = if (showDropdown) "Filtros" else "Buscar"
                )
            }
        }
    )
}

@Composable
@Preview(showBackground = true)
fun FilterLinePreview() {
    SnapGoalsTheme {
        FilterLine(
            query = "",
            onQueryChanged = {},
            onTrailingActionClick = {},
        )
    }

}