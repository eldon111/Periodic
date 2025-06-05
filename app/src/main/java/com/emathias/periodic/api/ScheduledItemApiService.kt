package com.emathias.periodic.api

import com.emathias.periodic.config.AppConfigService
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.model.converters.ScheduledItemJsonConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledItemApiService @Inject constructor(
    private val client: OkHttpClient,
    private val scheduledItemJsonConverter: ScheduledItemJsonConverter,
    private val appConfigService: AppConfigService,
) {
    private val contentType = "application/json".toMediaType()

    // Trigger for refreshing scheduled items
    private val _refreshTrigger = MutableStateFlow(0L)

    private suspend fun getBaseUrl(): String = appConfigService.getApiBaseUrl()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllScheduledItems(): Flow<List<ScheduledItem>> = _refreshTrigger.flatMapLatest {
        flow {
            val baseUrl = getBaseUrl()
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
                    items.add(scheduledItemJsonConverter.parseScheduledItem(jsonArray.getJSONObject(i)))
                }
                emit(items)
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun insertScheduledItem(json: JSONObject): Long = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl()
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
        val result = insertScheduledItem(
            scheduledItemJsonConverter.scheduledItemToJson(item)
        )
        refreshScheduledItems()
        result
    }

    /**
     * Triggers a refresh of the scheduled items list.
     * This will cause getAllScheduledItems() to re-fetch data from the API.
     */
    fun refreshScheduledItems() {
        _refreshTrigger.value = System.currentTimeMillis()
    }

    suspend fun updateScheduledItem(item: ScheduledItem) = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl()
        val requestBody = scheduledItemJsonConverter
            .scheduledItemToJson(item)
            .toString()
            .toRequestBody(contentType)

        val request = Request.Builder()
            .url("$baseUrl/scheduled-items/${item.id}")
            .put(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("API call failed with code ${response.code}")
        }
        refreshScheduledItems()
    }

    suspend fun deleteScheduledItem(id: Long) = withContext(Dispatchers.IO) {
        val baseUrl = getBaseUrl()
        val request = Request.Builder()
            .url("$baseUrl/scheduled-items/$id")
            .delete()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("API call failed with code ${response.code}")
        }
        refreshScheduledItems()
    }
}