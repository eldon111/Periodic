package com.emathias.periodic.config

import android.content.Context
import android.util.Log
import com.emathias.periodic.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationRequest
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appConfigClient: AppConfigDataClient,
) {
    private val sharedPrefs = context.getSharedPreferences("aws_config", Context.MODE_PRIVATE)

    suspend fun getApiBaseUrl(): String = withContext(Dispatchers.IO) {
        try {
            val request = StartConfigurationSessionRequest.builder()
                .applicationIdentifier("periodic-app")
                .environmentIdentifier(BuildConfig.ENVIRONMENT)
                .configurationProfileIdentifier("api-config")
                .build()

            val startConfigResponse = appConfigClient.startConfigurationSession(request)
            val latestConfigResponse = appConfigClient.getLatestConfiguration(
                GetLatestConfigurationRequest.builder()
                    .configurationToken(startConfigResponse.initialConfigurationToken())
                    .build()
            )
            val configContent = JSONObject(latestConfigResponse.configuration().asUtf8String())
            Log.d("AppConfigService", "Configuration content: $configContent")

            // Parse the configuration value - assuming it's just a plain URL string
            // In a real scenario, this might be JSON that needs parsing
            Log.d(
                "baseUrl",
                configContent.getJSONObject("baseUrl").getString("scheduledItemService")
            )
            configContent.getJSONObject("baseUrl").getString("scheduledItemService")
        } catch (e: Exception) {
            // Fallback to default URL if App Config is not available
            Log.e(
                "AppConfigService",
                "Failed to fetch API base URL from App Config, using default",
                e
            )
            "http://10.0.2.2:8080"
        }
    }
}