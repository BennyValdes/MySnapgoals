package com.mysnapgoals.app.ui.home

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mysnapgoals.app.R
import com.mysnapgoals.app.domain.model.GoalPeriodicity
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
fun EditGoalComponent(
    initialTitle: String,
    initialPeriodicity: GoalPeriodicity,
    initialDueDay: Long?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, periodicity: GoalPeriodicity, dueDay: Long) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var titleError by remember { mutableStateOf(false) }
    var periodicity by remember { mutableStateOf(initialPeriodicity) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDefaultDueConfirm by remember { mutableStateOf(false) }

    val titleFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val zoneId = remember { ZoneId.systemDefault() }
    val dateFormatter = remember {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
    }
    val initialDateMillis =
        remember(initialDueDay) {
            val date = LocalDate.ofEpochDay(initialDueDay ?: LocalDate.now().toEpochDay())
            date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        }
    var selectedDueDateMillis by remember(initialDateMillis) { mutableStateOf<Long?>(initialDateMillis) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    LaunchedEffect(Unit) {
        delay(100)
        titleFocusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(title) {
        if (titleError && title.isNotBlank()) titleError = false
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

    if (showDefaultDueConfirm) {
        AlertDialog(
            onDismissRequest = { showDefaultDueConfirm = false },
            title = { Text(stringResource(R.string.date_confirm_title)) },
            text = { Text(stringResource(R.string.date_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmedTitle = title.trim()
                        if (trimmedTitle.isBlank()) {
                            titleError = true
                            showDefaultDueConfirm = false
                            return@TextButton
                        }
                        val dueDay = defaultDueDay(periodicity)
                        focusManager.clearFocus()
                        showDefaultDueConfirm = false
                        onConfirm(trimmedTitle, periodicity, dueDay)
                    }
                ) { Text(stringResource(R.string.common_continue)) }
            },
            dismissButton = {
                TextButton(onClick = { showDefaultDueConfirm = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    AlertDialog(
        onDismissRequest = {
            focusManager.clearFocus()
            onDismiss()
        },
        title = { Text(stringResource(R.string.edit_goal_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocusRequester),
                    singleLine = true,
                    isError = titleError,
                    label = { Text(stringResource(R.string.common_title)) },
                    supportingText = { if (titleError) Text(stringResource(R.string.title_error)) }
                )

                Text(stringResource(R.string.periodicity_title))
                PeriodicityRadioRow(stringResource(R.string.periodicity_daily), periodicity == GoalPeriodicity.DAILY) {
                    periodicity = GoalPeriodicity.DAILY
                }
                PeriodicityRadioRow(stringResource(R.string.periodicity_weekly), periodicity == GoalPeriodicity.WEEKLY) {
                    periodicity = GoalPeriodicity.WEEKLY
                }
                PeriodicityRadioRow(stringResource(R.string.periodicity_monthly), periodicity == GoalPeriodicity.MONTHLY) {
                    periodicity = GoalPeriodicity.MONTHLY
                }
                PeriodicityRadioRow(stringResource(R.string.periodicity_semester), periodicity == GoalPeriodicity.SEMESTRAL) {
                    periodicity = GoalPeriodicity.SEMESTRAL
                }
                PeriodicityRadioRow(stringResource(R.string.periodicity_annual), periodicity == GoalPeriodicity.ANNUAL) {
                    periodicity = GoalPeriodicity.ANNUAL
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateMillis = selectedDueDateMillis ?: initialDueDay?.let { day ->
                        LocalDate.ofEpochDay(day).atStartOfDay(zoneId).toInstant().toEpochMilli()
                    }

                    val dateText =
                        dateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
                            date.format(dateFormatter)
                        } ?: stringResource(R.string.home_due_date_none)

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
                        val trimmedTitle = title.trim()
                        val validTitle = trimmedTitle.isNotBlank()
                        titleError = !validTitle
                        if (!validTitle) return@Button3D

                        val dueMillis = selectedDueDateMillis
                        val dueDay =
                            dueMillis?.let { millis ->
                                Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate().toEpochDay()
                            }?.coerceAtLeast(LocalDate.now().toEpochDay())

                        if (dueDay == null) {
                            showDefaultDueConfirm = true
                            return@Button3D
                        }

                        focusManager.clearFocus()
                        onConfirm(trimmedTitle, periodicity, dueDay)
                    },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                    depth = 4.dp
                ) { Text(stringResource(R.string.common_save)) }
            }
        }
    )
}

@Composable
private fun PeriodicityRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        RadioButton(selected = selected, onClick = onClick)
    }
}

private fun defaultDueDay(periodicity: GoalPeriodicity): Long {
    val today = LocalDate.now()
    return when (periodicity) {
        GoalPeriodicity.DAILY -> today.plusDays(1).toEpochDay()
        GoalPeriodicity.WEEKLY -> today.plusWeeks(1).toEpochDay()
        GoalPeriodicity.MONTHLY -> today.plusMonths(1).toEpochDay()
        GoalPeriodicity.SEMESTRAL -> today.plusMonths(6).toEpochDay()
        GoalPeriodicity.ANNUAL -> today.plusYears(1).toEpochDay()
    }
}
