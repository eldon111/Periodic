package com.emathias.periodic.model.converters

import android.util.Log
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.util.CronUtils
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledItemJsonConverter @Inject constructor() {

    companion object {
        private const val TAG = "ScheduledItemJsonConverter"
    }

    fun parseScheduledItem(json: JSONObject): ScheduledItem {
        val currentZone = ZoneId.systemDefault()
        return ScheduledItem(
            title = json.getString("title"),
            description = "",
            startsAt = LocalDateTime
                .parse(json.getString("startsAt"))
                .atZone(currentZone)
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
                LocalDateTime
                    .parse(json.getString("expiration"))
                    .atZone(currentZone)
                    .toInstant()
            },
        )
    }

    fun scheduledItemToJson(scheduledItem: ScheduledItem): JSONObject {
        val currentZone = ZoneId.systemDefault()
        val json = JSONObject()

        json.put("title", scheduledItem.title)
        json.put("description", scheduledItem.description)
        json.put(
            "startsAt",
            LocalDateTime.ofInstant(scheduledItem.startsAt, currentZone).toString()
        )
        json.put("repeats", scheduledItem.repeats)

        scheduledItem.cronExpression?.let { cron ->
            json.put("cronExpression", cron.asString())
        } ?: json.put("cronExpression", JSONObject.NULL)

        scheduledItem.expiration?.let { expiration ->
            json.put("expiration", LocalDateTime.ofInstant(expiration, currentZone).toString())
        } ?: json.put("expiration", JSONObject.NULL)

        return json
    }
}