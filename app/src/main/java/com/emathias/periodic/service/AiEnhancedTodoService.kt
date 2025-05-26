package com.emathias.periodic.service

import android.util.Log
import com.emathias.periodic.config.AwsCredentialManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiEnhancedTodoService @Inject constructor(
    private val bedrockService: BedrockAiService,
    private val credentialManager: AwsCredentialManager,
) {
    companion object {
        private const val TAG = "AiEnhancedTodoService"
    }

    /**
     * Generate smart suggestions for todo items based on user input
     */
    suspend fun generateTodoSuggestions(userInput: String): Result<List<String>> {
        if (!credentialManager.areCredentialsConfigured()) {
            return Result.failure(Exception("AWS credentials not configured"))
        }

        return try {
            val response = bedrockService.generateTextWithNova(
                prompt = userInput.trimIndent(),
                maxTokens = 300,
                temperature = 0.7f
            )

            if (response.isSuccess) {
                val suggestions = response.getOrNull()
                    ?.split("\n")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() && !it.startsWith("-") && !it.matches(Regex("^\\d+\\..*")) }
                    ?: emptyList()

                Log.d(TAG, "Generated ${suggestions.size} todo suggestions")
                Result.success(suggestions)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating todo suggestions", e)
            Result.failure(e)
        }
    }

    /**
     * Generate smart suggestions for todo items using Amazon Nova Lite
     */
    suspend fun generateTodoSuggestionsWithNova(userInput: String): Result<List<String>> {
        if (!credentialManager.areCredentialsConfigured()) {
            return Result.failure(Exception("AWS credentials not configured"))
        }

        return try {
            val response = bedrockService.generateTextWithNova(
                prompt = userInput.trimIndent(),
                maxTokens = 300,
                temperature = 0.7f
            )

            if (response.isSuccess) {
                val suggestions = response.getOrNull()
                    ?.split("\n")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() && !it.startsWith("-") && !it.matches(Regex("^\\d+\\..*")) }
                    ?: emptyList()

                Log.d(TAG, "Generated ${suggestions.size} todo suggestions with Nova")
                Result.success(suggestions)
            } else {
                Result.failure(response.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating todo suggestions with Nova", e)
            Result.failure(e)
        }
    }

    /**
     * Enhance a todo item description with AI
     */
    suspend fun enhanceTodoDescription(basicDescription: String): Result<String> {
        if (!credentialManager.areCredentialsConfigured()) {
            return Result.failure(Exception("AWS credentials not configured"))
        }

        return try {
            bedrockService.generateTextWithNova(
                prompt = basicDescription.trimIndent(),
                maxTokens = 150,
                temperature = 0.5f
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error enhancing todo description", e)
            Result.failure(e)
        }
    }

    /**
     * Generate a smart schedule suggestion for todo items
     */
    suspend fun generateScheduleSuggestion(todoItems: List<String>): Result<String> {
        if (!credentialManager.areCredentialsConfigured()) {
            return Result.failure(Exception("AWS credentials not configured"))
        }

        val itemsList = todoItems.joinToString("\n") { "- $it" }

        val prompt = """
            Given these todo items, suggest an optimal daily schedule:

            $itemsList

            Please provide:
            - A suggested order for completing these tasks
            - Estimated time for each task
            - Best time of day to do each task
            - Any dependencies between tasks

            Keep the response concise and practical.
        """.trimIndent()

        return try {
            bedrockService.generateTextWithNova(
                prompt = prompt,
                maxTokens = 400,
                temperature = 0.6f
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating schedule suggestion", e)
            Result.failure(e)
        }
    }

    /**
     * Check if AI features are available
     */
    fun isAiAvailable(): Boolean {
        return credentialManager.areCredentialsConfigured()
    }
}
