package com.emathias.periodic.api

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import com.emathias.periodic.db.entities.ScheduledItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledItemApiService @Inject constructor(
    private val client: OkHttpClient,
) {
    private val baseUrl = "http://10.0.2.2:8080"
    private val contentType = "application/json".toMediaType()
    private val cronParser = CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX))

    fun getAllScheduledItems(): Flow<List<ScheduledItem>> = flow {
        val request = Request.Builder()
            .url("$baseUrl/scheduled-items")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("API call failed with code ${response.code}")

            val body = response.body?.string() ?: throw Exception("Empty response body")
            val jsonArray = JSONArray(body)
            val items = mutableListOf<ScheduledItem>()

            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                items.add(jsonToScheduledItem(json))
            }
            emit(items)
        }
    }.flowOn(Dispatchers.IO)

    suspend fun insertScheduledItem(json: JSONObject): Long = withContext(Dispatchers.IO) {
        val requestBody = json.toString().toRequestBody(contentType)

        val request = Request.Builder()
            .url("$baseUrl/scheduled-items")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("API call failed with code ${response.code}")

            val body = response.body?.string() ?: throw Exception("Empty response body")
            JSONObject(body).getLong("id")
        }
    }

    suspend fun insertScheduledItem(item: ScheduledItem): Long = withContext(Dispatchers.IO) {
        val json = scheduledItemToJson(item)
        insertScheduledItem(json)
    }

    suspend fun updateScheduledItem(item: ScheduledItem) = withContext(Dispatchers.IO) {
        val json = scheduledItemToJson(item)
        val requestBody = json.toString().toRequestBody(contentType)

        val request = Request.Builder()
            .url("$baseUrl/scheduled-items/${item.id}")
            .put(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("API call failed with code ${response.code}")
        }
    }

    suspend fun deleteScheduledItem(id: Long) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/scheduled-items/$id")
            .delete()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("API call failed with code ${response.code}")
        }
    }

    private fun jsonToScheduledItem(json: JSONObject): ScheduledItem {
        return ScheduledItem(
            id = json.getLong("id"),
            title = json.getString("title"),
            description = json.optString("description", ""),
            startsAt = Instant.parse(json.getString("startsAt")),
            repeats = json.optBoolean("repeats", false),
            cronExpression = if (json.has("cronExpression") && !json.isNull("cronExpression")) {
                cronParser.parse(json.getString("cronExpression"))
            } else null,
            expiration = if (json.has("expiration") && !json.isNull("expiration")) {
                Instant.parse(json.getString("expiration"))
            } else null
        )
    }

    private fun scheduledItemToJson(item: ScheduledItem): JSONObject {
        return JSONObject().apply {
            put("id", item.id)
            put("title", item.title)
            put("description", item.description)
            put("startsAt", item.startsAt.toString())
            put("repeats", item.repeats)
            put("cronExpression", item.cronExpression?.asString())
            if (item.expiration != null) {
                put("expiration", item.expiration.toString())
            }
        }
    }
}