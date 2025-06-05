package com.emathias.periodic.config

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient
import java.time.Duration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AwsConfig {

    @Provides
    @Singleton
    fun provideAwsCredentials(@ApplicationContext context: Context): AwsBasicCredentials {
        // In production, you should store these securely (e.g., in encrypted SharedPreferences,
        // Android Keystore, or retrieve from a secure backend)
        val sharedPrefs = context.getSharedPreferences("aws_config", Context.MODE_PRIVATE)

        val accessKeyId = sharedPrefs.getString("aws_access_key_id", "") ?: ""
        val secretAccessKey = sharedPrefs.getString("aws_secret_access_key", "") ?: ""

        if (accessKeyId.isEmpty() || secretAccessKey.isEmpty()) {
            throw IllegalStateException(
                "AWS credentials not configured. Please set them in SharedPreferences."
            )
        }

        return AwsBasicCredentials.create(accessKeyId, secretAccessKey)
    }

    @Provides
    @Singleton
    fun provideAppConfigClient(credentials: AwsBasicCredentials): AppConfigDataClient {
        val clientConfig = ClientOverrideConfiguration.builder()
            .apiCallTimeout(Duration.ofSeconds(30))
            .apiCallAttemptTimeout(Duration.ofSeconds(10))
            .build()

        return AppConfigDataClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .overrideConfiguration(clientConfig)
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build()
    }
}
