package com.emathias.periodic.model

import com.emathias.periodic.util.RecurrenceFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ScheduleTest {

    private val zone = ZoneId.of("America/New_York")

    // Monday, Jan 1 2024, 9:00 AM
    private val start = ZonedDateTime.of(LocalDateTime.of(2024, 1, 1, 9, 0), zone)

    private fun schedule(rrule: String?) = Schedule(start, rrule)

    private fun at(y: Int, m: Int, d: Int, h: Int = 9, min: Int = 0) =
        ZonedDateTime.of(LocalDateTime.of(y, m, d, h, min), zone).toInstant()

    @Test
    fun serializeAndParse_roundTrips() {
        val original = schedule("FREQ=WEEKLY;INTERVAL=2;BYDAY=MO;UNTIL=20271231T235959Z")
        val text = original.serialize()
        assertEquals(
            "DTSTART;TZID=America/New_York:20240101T090000\n" +
                "RRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=MO;UNTIL=20271231T235959Z",
            text
        )
        assertEquals(original, Schedule.parse(text))
    }

    @Test
    fun serializeAndParse_roundTripsZoneIdsContainingColons() {
        listOf("+05:30", "GMT+05:30").forEach { zoneId ->
            val original = Schedule(
                ZonedDateTime.of(LocalDateTime.of(2024, 1, 1, 9, 0), ZoneId.of(zoneId))
            )
            assertEquals(original, Schedule.parse(original.serialize()))
        }
    }

    @Test
    fun parse_oneTimeAndInvalid() {
        val once = Schedule.parse("DTSTART;TZID=America/New_York:20240101T090000")
        assertNotNull(once)
        assertFalse(once!!.repeats)
        assertNull(Schedule.parse("garbage"))
        assertNull(Schedule.parse("DTSTART;TZID=America/New_York:20240101T090000\nRRULE:NOPE"))
    }

    @Test
    fun nextOccurrence_everyOtherWeek() {
        val s = schedule("FREQ=WEEKLY;INTERVAL=2;BYDAY=MO")
        assertEquals(at(2024, 1, 15), s.nextOccurrence(at(2024, 1, 1)))
        assertEquals(at(2024, 1, 15), s.nextOccurrence(at(2024, 1, 10)))
        assertEquals(at(2024, 1, 29), s.nextOccurrence(at(2024, 1, 15)))
    }

    @Test
    fun nextOccurrence_beforeStartDoesNotFastForward() {
        val s = schedule("FREQ=DAILY")
        assertEquals(start.toInstant(), s.nextOccurrence(Instant.MIN))
    }

    @Test
    fun nextOccurrence_everyOtherMonth() {
        val s = schedule("FREQ=MONTHLY;INTERVAL=2")
        assertEquals(at(2024, 3, 1), s.nextOccurrence(at(2024, 1, 1)))
        assertEquals(at(2024, 5, 1), s.nextOccurrence(at(2024, 3, 1)))
    }

    @Test
    fun nextOccurrence_nthWeekdayOfMonth() {
        val s = schedule("FREQ=MONTHLY;BYDAY=2TU")
        assertEquals(at(2024, 1, 9), s.nextOccurrence(at(2024, 1, 1)).also { /* second Tuesday */ })
    }

    @Test
    fun nextOccurrence_respectsDstWallClockTime() {
        val s = schedule("FREQ=DAILY")
        // Mar 10 2024 is the US spring-forward day; 9 AM local stays 9 AM local.
        assertEquals(at(2024, 3, 10), s.nextOccurrence(at(2024, 3, 9)))
    }

    @Test
    fun nextOccurrence_respectsCountAndUntil() {
        val counted = schedule("FREQ=DAILY;COUNT=3")
        assertEquals(at(2024, 1, 3), counted.nextOccurrence(at(2024, 1, 2)))
        assertNull(counted.nextOccurrence(at(2024, 1, 3)))

        val until = schedule("FREQ=DAILY;UNTIL=20240102T235959Z")
        assertEquals(at(2024, 1, 2), until.nextOccurrence(at(2024, 1, 1)))
        assertNull(until.nextOccurrence(at(2024, 1, 2)))
        assertTrue(until.until != null)
    }

    @Test
    fun nextOccurrence_oneTime() {
        val s = schedule(null)
        assertEquals(start.toInstant(), s.nextOccurrence(at(2023, 12, 31)))
        assertNull(s.nextOccurrence(start.toInstant()))
    }

    @Test
    fun formatter_describesSupportedRules() {
        fun d(rule: String?) = RecurrenceFormatter.describe(schedule(rule))
        assertEquals("Once on Jan 1, 2024 at 9:00 AM", d(null))
        assertEquals("Every day at 9:00 AM", d("FREQ=DAILY"))
        assertEquals("Every 3 days at 9:00 AM", d("FREQ=DAILY;INTERVAL=3"))
        assertEquals("Every other week on Monday, Wednesday at 9:00 AM", d("FREQ=WEEKLY;INTERVAL=2;BYDAY=WE,MO"))
        assertEquals("Every weekday at 9:00 AM", d("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"))
        assertEquals("Every other month on day 15 at 9:00 AM", d("FREQ=MONTHLY;INTERVAL=2;BYMONTHDAY=15"))
        assertEquals("Every month on the last day at 9:00 AM", d("FREQ=MONTHLY;BYMONTHDAY=-1"))
        assertEquals("Every month on the second Tuesday at 9:00 AM", d("FREQ=MONTHLY;BYDAY=2TU"))
        assertEquals("Every month on the last Friday at 9:00 AM", d("FREQ=MONTHLY;BYDAY=-1FR"))
        assertEquals("Every year on March 5 at 9:00 AM", d("FREQ=YEARLY;BYMONTH=3;BYMONTHDAY=5"))
        assertEquals("Every week on Monday at 9:00 AM, 10 times", d("FREQ=WEEKLY;BYDAY=MO;COUNT=10"))
        assertEquals(
            "Every week on Monday at 9:00 AM, until Dec 31, 2027",
            d("FREQ=WEEKLY;BYDAY=MO;UNTIL=20271231T235959Z")
        )
    }

    @Test
    fun formatter_fallsBackToCustom() {
        fun d(rule: String) = RecurrenceFormatter.describe(schedule(rule))
        assertEquals(RecurrenceFormatter.CUSTOM, d("FREQ=HOURLY"))
        assertEquals(RecurrenceFormatter.CUSTOM, d("FREQ=MONTHLY;BYDAY=MO,TU;BYSETPOS=-1"))
        assertEquals(RecurrenceFormatter.CUSTOM, d("FREQ=YEARLY;RSCALE=ISLAMIC-CIVIL"))
        assertEquals(RecurrenceFormatter.CUSTOM, d("FREQ=YEARLY;SKIP=BACKWARD"))
        assertEquals(RecurrenceFormatter.CUSTOM, d("FREQ=YEARLY;SKIP=FORWARD"))
        assertEquals(
            "Every year on January 1 at 9:00 AM",
            d("FREQ=YEARLY;RSCALE=GREGORIAN;SKIP=OMIT")
        )
    }
}
