package com.emathias.periodic.service

import android.content.Context
import android.util.Log
import com.emathias.periodic.R
import com.emathias.periodic.config.AwsCredentialManager
import com.emathias.periodic.db.entities.ScheduledItem
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Duration
import java.time.OffsetDateTime
import java.time.Period
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiEnhancedTodoService @Inject constructor(
    private val bedrockService: BedrockAiService,
    private val credentialManager: AwsCredentialManager,
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "AiEnhancedTodoService"
    }

    /**
     * Generate smart suggestions for todo items based on user input
     */
    suspend fun generateScheduledItem(userInput: String): Result<ScheduledItem> {
        if (!credentialManager.areCredentialsConfigured()) {
            return Result.failure(Exception("AWS credentials not configured"))
        }

        return try {
            val response = bedrockService.generateTextWithNova(
                systemPrompt = loadScheduledItemSystemPrompt(),
                prompt = userInput,
                maxTokens = 300,
                temperature = 0.7f
            )

            return response
                .map { JSONObject(it) }
                .map { json ->
                    ScheduledItem(
                        title = json.getString("title"),
                        description = "",
                        firstOccurrence = OffsetDateTime.parse(json.getString("firstOccurrence"))
                            .toInstant(),
                        repeats = json.getBoolean("repeats"),
                        intervalInMinutes = json.takeUnless { it.isNull("interval") }?.let {
                            parseIntervalInMinutes(json.getString("interval"))
                        },
                        expiration = json.takeUnless { it.isNull("expiration") }?.let {
                            OffsetDateTime.parse(json.getString("expiration")).toInstant()
                        },
                    )
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating scheduled item", e)
            Result.failure(e)
        }
    }

    private fun parseIntervalInMinutes(intervalString: String): Long {
        val parts = intervalString.split("T")
        println(parts)
        if (parts.size == 1) {
            return try {
//                println(Period.parse(parts[0]).days * 24L * 60L)
//                println(Period.parse(parts[0]).years)
//                println(Period.parse(parts[0]).months)
                Period.parse(parts[0]).days * 24L * 60L
            } catch (e: Exception) {
                println("Failed to parse period: $e")
                println(Duration.parse(parts[0]).toMinutes())
                Duration.parse(parts[0]).toMinutes()
            }
        }
        val period = Period.parse(parts[0])
        val duration = Duration.parse(parts[1])
        return period.days * 24L * 60L + duration.toMinutes()
    }

    /**
     * Check if AI features are available
     */
    fun isAiAvailable(): Boolean {
        return credentialManager.areCredentialsConfigured()
    }

    /**
     * Load system prompt from raw resource file
     */
    private fun loadScheduledItemSystemPrompt(): String {
        return try {
            val inputStream =
                context.resources.openRawResource(R.raw.new_scheduled_item_system_prompt)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.use { it.readText() }
            content
        } catch (e: Exception) {
            Log.e(TAG, "Error loading system prompt from resource", e)
            // Fallback to a basic prompt if resource loading fails
            "Generate a JSON object for a scheduled item based on user input."
        }
    }
}
