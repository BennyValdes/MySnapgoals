package com.mysnapgoals.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp

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
        },
        colors = OutlinedTextFieldDefaults.colors(
            // Contenedor: evita que se “coma” el borde por contraste
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,

            // Borde: define explícitamente ambos estados
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,

            // Opcional, pero mejora legibilidad
            focusedLabelColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
@Preview(showBackground = true)
fun FilterLinePreview() {
    FilterLine(
        query = "",
        onQueryChanged = {},
        onTrailingActionClick = {},
    )
}