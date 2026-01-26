package com.mysnapgoals.app.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.mysnapgoals.app.ui.theme.SnapGoalsTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalComponent(
    onDismiss: () -> Unit,
    onConfirm: (title: String, target: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf(false) }
    var targetError by remember { mutableStateOf(false) }

    val titleFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        delay(100)
        titleFocusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(title, targetText) {
        if (titleError && title.isNotBlank()) titleError = false
        if (targetError && targetText.isNotBlank()) targetError = false
    }

    AlertDialog(
        onDismissRequest = {
            focusManager.clearFocus()
            onDismiss()
        },
        title = { Text("Agregar Objetivo") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocusRequester),
                    singleLine = true,
                    isError = titleError,
                    label = { Text("Título") },
                    supportingText = { if (titleError) Text("El título no puede estar vacío") }
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { input -> targetText = input.filter { it.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = targetError,
                    label = { Text("Meta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { if (targetError) Text("La meta debe ser mayor a 0") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmedTitle = title.trim()
                    val target = targetText.toIntOrNull()

                    val validTitle = trimmedTitle.isNotBlank()
                    val validTarget = (target != null && target > 0)

                    titleError = !validTitle
                    targetError = !validTarget

                    if (!validTitle || !validTarget) return@TextButton

                    focusManager.clearFocus()
                    onConfirm(trimmedTitle, target!!)
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onDismiss()
                }
            ) { Text("Cancelar") }
        }
    )
}

@Composable
@Preview(showBackground = true)
fun AddGoalComponentPreview() {
    SnapGoalsTheme {
        AddGoalComponent(
            onDismiss = {},
            onConfirm = { _, _ -> },
        )
    }
}