package com.emathias.periodic.model.converters

import android.util.Log
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.util.CronUtils
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

    fun parseScheduledItem(json: JSONObject): ScheduledItem {
        return ScheduledItem(
            id = if (json.has("id")) json.getLong("id") else 0L,
            title = json.getString("title"),
            description = "",
            startsAt = OffsetDateTime
                .parse(json.getString("startsAt"))
                .toInstant(),
            repeats = json.getBoolean("repeats"),
            cronExpression = json.takeUnless { it.isNull("cronExpression") }?.let {
                val cronString = json.getString("cronExpression")
                CronUtils.parseCronExpression(cronString).also { cron ->
                    if (cron == null) {
                        Log.w(TAG, "Failed to parse cron expression: $cronString")
                    }
                }
            },
            expiration = json.takeUnless { it.isNull("expiration") }?.let {
                OffsetDateTime
                    .parse(json.getString("expiration"))
                    .toInstant()
            },
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
        json.put("repeats", scheduledItem.repeats)

        scheduledItem.cronExpression?.let { cron ->
            json.put("cronExpression", cron.asString())
        } ?: json.put("cronExpression", JSONObject.NULL)

        scheduledItem.expiration?.let { expiration ->
            json.put(
                "expiration",
                expiration.atOffset(ZoneOffset.UTC).format(DATE_TIME_FORMATTER)
            )
        } ?: json.put("expiration", JSONObject.NULL)

        return json
    }
}