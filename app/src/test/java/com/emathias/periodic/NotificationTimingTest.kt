package com.emathias.periodic

import com.emathias.periodic.notification.NotificationScheduler
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class NotificationTimingTest {
    private val zone = ZoneId.of("America/New_York")

    @Test
    fun delayBeforeReminderTimeIsSameDay() {
        val now = ZonedDateTime.of(2026, 10, 19, 6, 0, 0, 0, zone)
        assertEquals(Duration.ofHours(2), NotificationScheduler.delayUntilNext(LocalTime.of(8, 0), now))
    }

    @Test
    fun delayAfterReminderTimeIsNextDay() {
        val now = ZonedDateTime.of(2026, 10, 19, 9, 0, 0, 0, zone)
        assertEquals(Duration.ofHours(23), NotificationScheduler.delayUntilNext(LocalTime.of(8, 0), now))
    }
}
