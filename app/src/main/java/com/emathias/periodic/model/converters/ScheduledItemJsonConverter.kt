package com.emathias.periodic.model.converters

import android.util.Log
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.model.Schedule
import org.json.JSONObject
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledItemJsonConverter @Inject constructor() {

    companion object {
        private const val TAG = "ScheduledItemJsonConverter"
        private val DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
    }

    /**
     * Expects `startsAt` (ISO offset date-time) and an optional `rrule` (RFC 5545 RRULE value,
     * with end date / count expressed through UNTIL / COUNT inside the rule).
     */
    fun parseScheduledItem(json: JSONObject): ScheduledItem {
        val startsAt = OffsetDateTime.parse(json.getString("startsAt")).toInstant()
        val rrule = json.takeUnless { it.isNull("rrule") }?.getString("rrule")
        val schedule = Schedule.of(startsAt, rrule)
            ?: run {
                Log.w(TAG, "Failed to parse recurrence rule: $rrule")
                Schedule.oneTime(startsAt)
            }
        return ScheduledItem(
            id = if (json.has("id")) json.getLong("id") else 0L,
            title = json.getString("title"),
            description = "",
            schedule = schedule,
        )
    }

    fun scheduledItemToJson(scheduledItem: ScheduledItem): JSONObject {
        val json = JSONObject()

        json.put("title", scheduledItem.title)
        json.put("description", scheduledItem.description)
        json.put(
            "startsAt",
            scheduledItem.startsAt.atOffset(ZoneOffset.UTC).format(DATE_TIME_FORMATTER)
        )
        json.put("rrule", scheduledItem.schedule.rrule ?: JSONObject.NULL)

        return json
    }
}
