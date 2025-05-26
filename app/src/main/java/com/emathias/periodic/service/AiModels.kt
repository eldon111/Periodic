package com.emathias.periodic.service

/**
 * Data classes for AI service responses and configurations
 */

data class AiResponse(
    val text: String,
    val modelUsed: String,
    val tokensUsed: Int? = null,
    val processingTimeMs: Long? = null,
)

data class AiGenerationConfig(
    val maxTokens: Int = 512,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val modelId: String = BedrockAiService.AMAZON_NOVA_LITE,
)

sealed class AiServiceError : Exception() {
    object NotConfigured : AiServiceError()
    object NetworkError : AiServiceError()
    object AuthenticationError : AiServiceError()
    object ModelNotAvailable : AiServiceError()
    data class ApiError(val code: String, override val message: String) : AiServiceError()
    data class UnknownError(override val message: String) : AiServiceError()
}
