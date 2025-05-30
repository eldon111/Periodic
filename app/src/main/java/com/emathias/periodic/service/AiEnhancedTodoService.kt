package com.emathias.periodic.service

import android.content.Context
import android.util.Log
import com.emathias.periodic.R
import com.emathias.periodic.config.AwsCredentialManager
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.util.CronUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

        // Get current timezone and date/time information
        val currentZone = ZoneId.systemDefault()
        val currentDateTime = OffsetDateTime.now(currentZone)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val extraInfo =
            """
            |
            |
            |
            |Additional context:
            | Current timezone: ${currentZone.id},
            | Current date and time: ${currentDateTime.format(formatter)}
            """.trimMargin()

        return try {
            val response = bedrockService.generateTextWithNova(
                systemPrompt = loadScheduledItemSystemPrompt(),
                prompt = userInput + extraInfo,
                maxTokens = 300,
                temperature = 0.7f
            )

            return response
                .map { JSONObject(it) }
                .map { json ->
                    ScheduledItem(
                        title = json.getString("title"),
                        description = "",
                        firstOccurrence = LocalDateTime
                            .parse(json.getString("firstOccurrence"))
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
        } catch (e: Exception) {
            Log.e(TAG, "Error generating scheduled item", e)
            Result.failure(e)
        }
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
