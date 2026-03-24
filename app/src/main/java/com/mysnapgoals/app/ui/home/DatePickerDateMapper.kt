package com.mysnapgoals.app.ui.home

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

internal fun epochDayToPickerMillis(epochDay: Long): Long {
    return LocalDate.ofEpochDay(epochDay)
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()
}

internal fun pickerMillisToEpochDay(millis: Long): Long {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toEpochDay()
}

internal fun pickerMillisToLocalDate(millis: Long): LocalDate {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
}
