package com.budgettracker.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DateUtilsTest {
    private val anchorDate = LocalDate.of(2026, 10, 1)

    @Test
    fun testFormatDayNavigation_relativeDates() {
        val todayStr = DateUtils.formatDayNavigation(anchorDate, today = anchorDate)
        assertEquals("Today, Oct 1", todayStr)

        val yesterday = anchorDate.minusDays(1)
        val yesterdayStr = DateUtils.formatDayNavigation(yesterday, today = anchorDate)
        assertEquals("Yesterday, Sep 30", yesterdayStr)

        val tomorrow = anchorDate.plusDays(1)
        val tomorrowStr = DateUtils.formatDayNavigation(tomorrow, today = anchorDate)
        assertEquals("Tomorrow, Oct 2", tomorrowStr)
    }

    @Test
    fun testFormatMonthYear() {
        val formatted = DateUtils.formatMonthYear(2026, 10)
        assertEquals("October 2026", formatted)
    }

    @Test
    fun testGetShortMonthName() {
        assertEquals("Jan", DateUtils.getShortMonthName(1))
        assertEquals("Oct", DateUtils.getShortMonthName(10))
        assertEquals("Dec", DateUtils.getShortMonthName(12))
    }

    @Test
    fun testFormatTime_utc() {
        val millis = Instant.parse("2026-10-01T14:30:00Z").toEpochMilli()
        val formattedTime = DateUtils.formatTime(millis, zoneId = ZoneId.of("UTC"))
        assertEquals("2:30 PM", formattedTime)
    }
}
