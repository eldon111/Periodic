package com.emathias.periodic.config

import android.content.Context
import androidx.room.Room
import com.emathias.periodic.db.dao.AppDatabase
import com.emathias.periodic.db.dao.ScheduledItemDao
import com.emathias.periodic.db.dao.ScheduledItemHistoryDao
import com.emathias.periodic.db.dao.ScheduledItemProcessHistoryDao
import com.emathias.periodic.db.dao.TodoItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RoomModule {

    @Provides
    fun providesAppDatabase(@ApplicationContext applicationContext: Context): AppDatabase {
        return Room
            .databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "todolist.db"
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun providesScheduledItemDao(appDatabase: AppDatabase): ScheduledItemDao {
        return appDatabase.scheduledItemDao()
    }

    @Provides
    fun providesScheduledItemHistoryDao(appDatabase: AppDatabase): ScheduledItemHistoryDao {
        return appDatabase.scheduledItemHistoryDao()
    }

    @Provides
    fun providesScheduledItemProcessHistoryDao(appDatabase: AppDatabase): ScheduledItemProcessHistoryDao {
        return appDatabase.scheduledItemProcessHistoryDao()
    }

    @Provides
    fun todoItemDao(appDatabase: AppDatabase): TodoItemDao {
        return appDatabase.todoItemDao()
    }
}