package com.mysnapgoals.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mysnapgoals.app.R
import com.mysnapgoals.app.ui.components.Button3D
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTodoComponent(
    initialTitle: String,
    initialScheduledDay: Long?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, scheduledDay: Long) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var hasError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val zoneId = remember { ZoneId.systemDefault() }
    val dateFormatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    }
    val initialDateMillis =
        remember(initialScheduledDay) {
            val date = LocalDate.ofEpochDay(initialScheduledDay ?: LocalDate.now().toEpochDay())
            date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        }
    var selectedDueDateMillis by remember { mutableStateOf<Long?>(initialDateMillis) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(title) {
        if (hasError && title.isNotBlank()) hasError = false
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDueDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) { Text(stringResource(R.string.common_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = {
            focusManager.clearFocus()
            onDismiss()
        },
        title = { Text(stringResource(R.string.edit_todo_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    isError = hasError,
                    label = { Text(stringResource(R.string.common_title)) },
                    supportingText = { if (hasError) Text(stringResource(R.string.title_error)) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dateText =
                        selectedDueDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
                            date.format(dateFormatter)
                        } ?: stringResource(R.string.home_due_date_today)

                    Text(text = stringResource(R.string.home_due_date, dateText))
                    Spacer(modifier = Modifier.weight(1f))
                    Button3D(
                        onClick = { showDatePicker = true },
                        height = 36.dp,
                        depth = 3.dp
                    ) { Text(stringResource(R.string.common_calendar)) }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button3D(
                    onClick = {
                        focusManager.clearFocus()
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                    depth = 4.dp
                ) { Text(stringResource(R.string.common_cancel)) }

                Button3D(
                    onClick = {
                        val trimmed = title.trim()
                        if (trimmed.isBlank()) {
                            hasError = true
                            return@Button3D
                        }
                        val scheduledDay =
                            selectedDueDateMillis?.let { millis ->
                                Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate().toEpochDay()
                            } ?: LocalDate.now().toEpochDay()
                        focusManager.clearFocus()
                        onConfirm(trimmed, scheduledDay)
                    },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                    depth = 4.dp
                ) { Text(stringResource(R.string.common_save)) }
            }
        }
    )
}
