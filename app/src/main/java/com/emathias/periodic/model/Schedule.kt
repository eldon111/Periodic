package com.emathias.periodic.model

import org.dmfs.rfc5545.DateTime
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException
import org.dmfs.rfc5545.recur.RecurrenceRule
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

/**
 * A one-time or repeating schedule. Persisted as a single iCalendar-style string:
 *
 * ```
 * DTSTART;TZID=America/New_York:20261017T090000
 * RRULE:FREQ=WEEKLY;INTERVAL=2;BYDAY=MO;UNTIL=20271231T235959Z
 * ```
 *
 * The start lives in DTSTART; end date / occurrence count live in the RRULE (UNTIL / COUNT).
 * Without an RRULE the schedule is a single occurrence at [start].
 */
data class Schedule(
    val start: ZonedDateTime,
    val rrule: String? = null,
) {
    val repeats: Boolean get() = rrule != null

    val recurrenceRule: RecurrenceRule? by lazy {
        rrule?.let { parseRule(it) }
    }

    /** The end of the recurrence (UNTIL), if the rule has one. */
    val until: Instant?
        get() = recurrenceRule?.until?.let { Instant.ofEpochMilli(it.timestamp) }

    /** First occurrence at or after [start]. */
    val firstOccurrence: Instant get() = start.toInstant()

    /** The next occurrence strictly after [after], or null if there is none. */
    fun nextOccurrence(after: Instant): Instant? {
        val rule = recurrenceRule
        if (rule == null) {
            return start.toInstant().takeIf { it.isAfter(after) }
        }
        val dtStart = start.toLibDateTime()
        val iterator = rule.iterator(dtStart)
        iterator.fastForward(DateTime(dtStart.timeZone, after.toEpochMilli()))
        while (iterator.hasNext()) {
            val candidate = Instant.ofEpochMilli(iterator.nextMillis())
            if (candidate.isAfter(after)) return candidate
        }
        return null
    }

    fun serialize(): String = buildString {
        append("DTSTART;TZID=").append(start.zone.id).append(':')
        append(start.toLocalDateTime().format(LOCAL_FORMAT))
        rrule?.let { append('\n').append("RRULE:").append(it) }
    }

    companion object {
        private val LOCAL_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")

        fun oneTime(start: Instant, zone: ZoneId = ZoneId.systemDefault()) =
            Schedule(start.atZone(zone))

        private fun parseRule(rrule: String): RecurrenceRule? = try {
            RecurrenceRule(rrule, RecurrenceRule.RfcMode.RFC5545_LAX)
        } catch (e: InvalidRecurrenceRuleException) {
            null
        }

        fun isValidRule(rrule: String): Boolean = parseRule(rrule.removePrefix("RRULE:")) != null

        /** Parses [serialize]d text; returns null if it is malformed. */
        fun parse(text: String): Schedule? {
            var start: ZonedDateTime? = null
            var rrule: String? = null
            for (line in text.lines().map { it.trim() }.filter { it.isNotEmpty() }) {
                when {
                    line.startsWith("DTSTART") -> start = parseDtStart(line) ?: return null
                    line.startsWith("RRULE:") -> rrule = line.removePrefix("RRULE:")
                    else -> return null
                }
            }
            if (start == null) return null
            if (rrule != null && !isValidRule(rrule)) return null
            return Schedule(start, rrule)
        }

        /** Builds a schedule from a bare RRULE value (optionally prefixed with "RRULE:"). */
        fun of(start: Instant, rrule: String?, zone: ZoneId = ZoneId.systemDefault()): Schedule? {
            val rule = rrule?.trim()?.removePrefix("RRULE:")?.takeIf { it.isNotEmpty() }
            if (rule != null && !isValidRule(rule)) return null
            return Schedule(start.atZone(zone), rule)
        }

        private fun parseDtStart(line: String): ZonedDateTime? = try {
            val (params, value) = line.substringAfter("DTSTART").split(":", limit = 2)
                .let { it[0] to it.getOrNull(1) }
            if (value == null) {
                null
            } else if (value.endsWith("Z")) {
                LocalDateTime.parse(value.removeSuffix("Z"), LOCAL_FORMAT).atZone(ZoneOffset.UTC)
            } else {
                val zone = params.removePrefix(";TZID=").takeIf { params.startsWith(";TZID=") }
                    ?.let { ZoneId.of(it) } ?: ZoneId.systemDefault()
                LocalDateTime.parse(value, LOCAL_FORMAT).atZone(zone)
            }
        } catch (e: Exception) {
            null
        }

    }
}

private fun ZonedDateTime.toLibDateTime() = DateTime(
    TimeZone.getTimeZone(zone),
    year, monthValue - 1, dayOfMonth, hour, minute, second,
)
