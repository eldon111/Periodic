package com.emathias.periodic.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset

class RepetitionTest {
    private val start = LocalDate.of(2026, 10, 14) // second Wednesday

    @Test
    fun dailyEveryOtherDay() {
        assertEquals("FREQ=DAILY;INTERVAL=2", Repetition(RepeatUnit.DAY, 2).toRrule(start))
    }

    @Test
    fun weeklyWithDays() {
        val rule = Repetition(RepeatUnit.WEEK, weekdays = setOf(DayOfWeek.FRIDAY, DayOfWeek.MONDAY)).toRrule(start)
        assertEquals("FREQ=WEEKLY;BYDAY=MO,FR", rule)
    }

    @Test
    fun monthlyModes() {
        assertEquals("FREQ=MONTHLY;BYMONTHDAY=14", Repetition(RepeatUnit.MONTH).toRrule(start))
        assertEquals(
            "FREQ=MONTHLY;BYDAY=2WE",
            Repetition(RepeatUnit.MONTH, monthlyMode = MonthlyMode.NTH_WEEKDAY).toRrule(start),
        )
        assertEquals(
            "FREQ=MONTHLY;BYMONTHDAY=-1",
            Repetition(RepeatUnit.MONTH, monthlyMode = MonthlyMode.LAST_DAY).toRrule(start),
        )
    }

    @Test
    fun endDateAndValidity() {
        val rule = Repetition(RepeatUnit.YEAR, endDate = LocalDate.of(2030, 1, 1)).toRrule(start, ZoneOffset.UTC)
        assertEquals("FREQ=YEARLY;UNTIL=20300101T235959Z", rule)
        assertTrue(Schedule.isValidRule(rule))
    }
}
