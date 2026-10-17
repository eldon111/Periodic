package com.emathias.periodic.config

import com.emathias.periodic.model.converters.ScheduledItemJsonConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun providesScheduledItemJsonConverter(): ScheduledItemJsonConverter {
        return ScheduledItemJsonConverter()
    }
}
