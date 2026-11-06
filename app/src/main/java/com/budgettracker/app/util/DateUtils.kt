package com.budgettracker.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    private val dayHeaderFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.US)
    private val fullDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US)
    private val shortMonthDayFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    fun formatDayNavigation(date: LocalDate, today: LocalDate = LocalDate.now()): String {
        return when (date) {
            today -> "Today, ${date.format(shortMonthDayFormatter)}"
            today.minusDays(1) -> "Yesterday, ${date.format(shortMonthDayFormatter)}"
            today.plusDays(1) -> "Tomorrow, ${date.format(shortMonthDayFormatter)}"
            else -> date.format(dayHeaderFormatter)
        }
    }

    fun formatFullDate(date: LocalDate): String {
        return date.format(fullDateFormatter)
    }

    fun formatTime(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val localTime = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalTime()
        return localTime.format(timeFormatter)
    }

    fun formatMonthYear(year: Int, month: Int): String {
        val monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.US)
        return "$monthName $year"
    }

    fun getShortMonthName(month: Int): String {
        return Month.of(month).getDisplayName(TextStyle.SHORT, Locale.US)
    }
}
