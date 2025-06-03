package com.emathias.periodic.model.converters

import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.util.CronUtils
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ScheduledItemJsonConverterTest {

    private lateinit var converter: ScheduledItemJsonConverter

    @Before
    fun setUp() {
        converter = ScheduledItemJsonConverter()
    }

    @Test
    fun parseScheduledItem_withCompleteData_parsesCorrectly() {
        val json = JSONObject().apply {
            put("title", "Test Task")
            put("startsAt", "2024-01-15T10:30:00")
            put("repeats", true)
            put("cronExpression", "0 30 10 * * ?")
            put("expiration", "2024-12-31T23:59:59")
        }

        val result = converter.parseScheduledItem(json)

        assertEquals("Test Task", result.title)
        assertEquals("", result.description)
        assertTrue(result.repeats)
        assertNotNull(result.cronExpression)
        assertNotNull(result.expiration)

        val expectedStartsAt = LocalDateTime.parse("2024-01-15T10:30:00")
            .atZone(ZoneId.systemDefault()).toInstant()
        assertEquals(expectedStartsAt, result.startsAt)

        val expectedExpiration = LocalDateTime.parse("2024-12-31T23:59:59")
            .atZone(ZoneId.systemDefault()).toInstant()
        assertEquals(expectedExpiration, result.expiration)
    }

    @Test
    fun parseScheduledItem_withNullOptionalFields_parsesCorrectly() {
        val json = JSONObject().apply {
            put("title", "Simple Task")
            put("startsAt", "2024-01-15T10:30:00")
            put("repeats", false)
            put("cronExpression", JSONObject.NULL)
            put("expiration", JSONObject.NULL)
        }

        val result = converter.parseScheduledItem(json)

        assertEquals("Simple Task", result.title)
        assertEquals("", result.description)
        assertFalse(result.repeats)
        assertNull(result.cronExpression)
        assertNull(result.expiration)
    }

    @Test
    fun parseScheduledItem_withInvalidCronExpression_setsNullCronExpression() {
        val json = JSONObject().apply {
            put("title", "Invalid Cron Task")
            put("startsAt", "2024-01-15T10:30:00")
            put("repeats", true)
            put("cronExpression", "invalid cron")
            put("expiration", JSONObject.NULL)
        }

        val result = converter.parseScheduledItem(json)

        assertEquals("Invalid Cron Task", result.title)
        assertTrue(result.repeats)
        assertNull(result.cronExpression)
    }

    @Test
    fun scheduledItemToJson_withCompleteData_convertsCorrectly() {
        val cronExpression = CronUtils.parseCronExpression("0 30 10 * * ?")
        val startsAt = LocalDateTime.parse("2024-01-15T10:30:00")
            .atZone(ZoneId.systemDefault()).toInstant()
        val expiration = LocalDateTime.parse("2024-12-31T23:59:59")
            .atZone(ZoneId.systemDefault()).toInstant()

        val scheduledItem = ScheduledItem(
            id = 1L,
            title = "Test Task",
            description = "Test Description",
            startsAt = startsAt,
            repeats = true,
            cronExpression = cronExpression,
            expiration = expiration
        )

        val result = converter.scheduledItemToJson(scheduledItem)

        assertEquals("Test Task", result.getString("title"))
        assertEquals("Test Description", result.getString("description"))
        assertEquals("2024-01-15T10:30:00", result.getString("startsAt"))
        assertTrue(result.getBoolean("repeats"))
        assertEquals("0 30 10 * * ?", result.getString("cronExpression"))
        assertEquals("2024-12-31T23:59:59", result.getString("expiration"))
    }

    @Test
    fun scheduledItemToJson_withNullOptionalFields_convertsCorrectly() {
        val startsAt = LocalDateTime.parse("2024-01-15T10:30:00")
            .atZone(ZoneId.systemDefault()).toInstant()

        val scheduledItem = ScheduledItem(
            id = 1L,
            title = "Simple Task",
            description = "",
            startsAt = startsAt,
            repeats = false,
            cronExpression = null,
            expiration = null
        )

        val result = converter.scheduledItemToJson(scheduledItem)

        assertEquals("Simple Task", result.getString("title"))
        assertEquals("", result.getString("description"))
        assertEquals("2024-01-15T10:30:00", result.getString("startsAt"))
        assertFalse(result.getBoolean("repeats"))
        assertTrue(result.isNull("cronExpression"))
        assertTrue(result.isNull("expiration"))
    }

    @Test
    fun roundTripConversion_preservesData() {
        val originalJson = JSONObject().apply {
            put("title", "Round Trip Task")
            put("startsAt", "2024-01-15T10:30:00")
            put("repeats", true)
            put("cronExpression", "0 30 10 * * ?")
            put("expiration", "2024-12-31T23:59:59")
        }

        val scheduledItem = converter.parseScheduledItem(originalJson)
        val convertedJson = converter.scheduledItemToJson(scheduledItem)

        assertEquals(originalJson.getString("title"), convertedJson.getString("title"))
        assertEquals(originalJson.getString("startsAt"), convertedJson.getString("startsAt"))
        assertEquals(originalJson.getBoolean("repeats"), convertedJson.getBoolean("repeats"))
        assertEquals(
            originalJson.getString("cronExpression"),
            convertedJson.getString("cronExpression")
        )
        assertEquals(originalJson.getString("expiration"), convertedJson.getString("expiration"))
    }

    @Test
    fun roundTripConversion_withNullFields_preservesData() {
        val originalJson = JSONObject().apply {
            put("title", "Simple Round Trip")
            put("startsAt", "2024-01-15T10:30:00")
            put("repeats", false)
            put("cronExpression", JSONObject.NULL)
            put("expiration", JSONObject.NULL)
        }

        val scheduledItem = converter.parseScheduledItem(originalJson)
        val convertedJson = converter.scheduledItemToJson(scheduledItem)

        assertEquals(originalJson.getString("title"), convertedJson.getString("title"))
        assertEquals(originalJson.getString("startsAt"), convertedJson.getString("startsAt"))
        assertEquals(originalJson.getBoolean("repeats"), convertedJson.getBoolean("repeats"))
        assertTrue(convertedJson.isNull("cronExpression"))
        assertTrue(convertedJson.isNull("expiration"))
    }
}