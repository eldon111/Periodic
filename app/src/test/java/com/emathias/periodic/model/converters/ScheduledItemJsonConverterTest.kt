package com.emathias.periodic.model.converters

import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.model.Schedule
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class ScheduledItemJsonConverterTest {

    private lateinit var converter: ScheduledItemJsonConverter

    @Before
    fun setUp() {
        converter = ScheduledItemJsonConverter()
    }

    @Test
    fun parseScheduledItem_withRule_parsesCorrectly() {
        val json = JSONObject().apply {
            put("title", "Test Task")
            put("startsAt", "2024-01-15T10:30:00Z")
            put("rrule", "FREQ=WEEKLY;INTERVAL=2;BYDAY=MO;UNTIL=20241231T235959Z")
        }

        val result = converter.parseScheduledItem(json)

        assertEquals("Test Task", result.title)
        assertTrue(result.repeats)
        assertEquals(Instant.parse("2024-01-15T10:30:00Z"), result.startsAt)
        assertEquals(Instant.parse("2024-12-31T23:59:59Z"), result.expiration)
    }

    @Test
    fun parseScheduledItem_withoutRule_isOneTime() {
        val json = JSONObject().apply {
            put("title", "Simple Task")
            put("startsAt", "2024-01-15T10:30:00Z")
            put("rrule", JSONObject.NULL)
        }

        val result = converter.parseScheduledItem(json)

        assertFalse(result.repeats)
        assertNull(result.expiration)
    }

    @Test
    fun parseScheduledItem_withInvalidRule_fallsBackToOneTime() {
        val json = JSONObject().apply {
            put("title", "Bad")
            put("startsAt", "2024-01-15T10:30:00Z")
            put("rrule", "invalid")
        }

        assertFalse(converter.parseScheduledItem(json).repeats)
    }

    @Test
    fun roundTrip_preservesData() {
        val item = ScheduledItem(
            title = "Test",
            description = "desc",
            schedule = Schedule.of(Instant.parse("2024-01-15T10:30:00Z"), "FREQ=MONTHLY;BYDAY=2TU;COUNT=5")!!,
        )

        val json = converter.scheduledItemToJson(item)
        val parsed = converter.parseScheduledItem(json)

        assertEquals("FREQ=MONTHLY;BYDAY=2TU;COUNT=5", json.getString("rrule"))
        assertEquals("2024-01-15T10:30:00Z", json.getString("startsAt"))
        assertEquals(item.schedule.rrule, parsed.schedule.rrule)
        assertEquals(item.startsAt, parsed.startsAt)
    }
}
