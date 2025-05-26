package com.emathias.periodic.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BedrockAiService @Inject constructor(
    private val bedrockClient: BedrockRuntimeClient,
) {
    companion object {
        private const val TAG = "BedrockAiService"

        // Available model IDs
        const val AMAZON_NOVA_LITE = "amazon.nova-lite-v1:0"
        const val SYSTEM_PROMPT = """
Your role is to generate a json object representing a scheduled item based on a user request.

Only take into account the most recent user message when building context.

The user will request an item to be generated and may supply details like date and time, whether it repeats, at what interval, etc.

Your response should only be a single json object, with no other output of any kind.
DO NOT wrap the JSON in markdown code blocks, quotes, or any other formatting.
DO NOT include ```json, ```, or any other markdown syntax.
ONLY return the raw JSON object itself.

The following is a template of the format required for the json object:
{
    "title":"<user-requested title or generated title based on description>",
    "firstOccurrence":<ISO datetime in UTC with the format yyyy-MM-ddTHH:mm:ssZ, taking the user's timezone into account>,
    "repeats":<boolean, defaults to false>,
    "interval":<you can leave this property out entirely if 'repeats' is false, otherwise ISO 8601 duration (format: P(n)Y(n)M(n)DT(n)H(n)M(n)S)>
}
"""
    }

    /**
     * Generate text using Amazon Nova Lite model
     */
    suspend fun generateTextWithNova(
        prompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.0f,
        topP: Float = 0.0f,
        topK: Int = 20,
        modelId: String = AMAZON_NOVA_LITE,
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // For Nova Lite, we need to combine system prompt and user prompt
            // as it doesn't support system role
            val combinedPrompt = "$SYSTEM_PROMPT\n\nUser input: $prompt"

            val requestBody = JSONObject().apply {
                put("schemaVersion", "messages-v1")
                put("messages", JSONArray().apply {
                    // Single user message with combined prompts
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", combinedPrompt)
                            })
                        })
                    })
                })
                put("inferenceConfig", JSONObject().apply {
                    put("maxTokens", maxTokens)
                    put("temperature", temperature)
                    put("topP", topP)
                    put("topK", topK)
                })
            }

            val response = invokeModel(modelId, requestBody.toString())
            val responseBody = JSONObject(response)
            val output = responseBody.getJSONObject("output")
            val message = output.getJSONObject("message")
            val content = message.getJSONArray("content")
            val text = content.getJSONObject(0).getString("text")
            Log.d(TAG, "Generated text: $text")

            val json = JSONObject(text)
            Log.d(TAG, "Generated json: $json")

            Result.success(text.trim())
        } catch (e: Exception) {
            Log.e(TAG, "Error generating text with Nova", e)
            Result.failure(e)
        }
    }

    /**
     * Generic method to invoke any Bedrock model
     */
    private suspend fun invokeModel(modelId: String, requestBody: String): String =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Invoking model: $modelId")
            Log.d(TAG, "Request body: $requestBody")

            val request = InvokeModelRequest.builder()
                .modelId(modelId)
                .body(SdkBytes.fromUtf8String(requestBody))
                .contentType("application/json")
                .accept("application/json")
                .build()

            val response: InvokeModelResponse = bedrockClient.invokeModel(request)
            val responseBody = response.body().asUtf8String()

            Log.d(TAG, "Response: $responseBody")
            return@withContext responseBody
        }

    /**
     * Test connection to Bedrock service
     */
    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Try a simple request to test the connection
            generateTextWithNova("Hello", maxTokens = 10)
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            Result.failure(e)
        }
    }
}
