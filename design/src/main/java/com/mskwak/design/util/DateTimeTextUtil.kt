package com.mskwak.design.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

fun LocalDate?.toYearMonthString(): String {
    return this?.format(DateTimeFormatter.ofPattern("yyyy. MM")) ?: ""
}

fun LocalDate?.toDateString(): String {
    return this?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: ""
}

fun LocalTime?.toTimeString(): String {
    return this?.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) ?: ""
}

fun LocalDate?.toDateWithDayOfWeek(): String {
    return this?.format(DateTimeFormatter.ofPattern("MM. dd E")) ?: ""
}

