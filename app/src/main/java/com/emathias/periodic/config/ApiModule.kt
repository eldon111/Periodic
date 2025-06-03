package com.emathias.periodic.config

import com.emathias.periodic.api.ScheduledItemApiService
import com.emathias.periodic.model.converters.ScheduledItemJsonConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun providesScheduledItemJsonConverter(): ScheduledItemJsonConverter {
        return ScheduledItemJsonConverter()
    }

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun providesScheduledItemApiService(
        client: OkHttpClient,
        scheduledItemJsonConverter: ScheduledItemJsonConverter,
    ): ScheduledItemApiService {
        return ScheduledItemApiService(client, scheduledItemJsonConverter)
    }
}