package com.emathias.periodic.db.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emathias.periodic.db.converters.InstantConverter
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.db.entities.ScheduledItemHistory
import com.emathias.periodic.db.entities.ScheduledItemProcessHistory
import com.emathias.periodic.db.entities.TodoItem

@Database(
    entities = [
        TodoItem::class,
        ScheduledItem::class,
        ScheduledItemHistory::class,
        ScheduledItemProcessHistory::class,
    ],
    version = 4,
    exportSchema = false,
//    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
@TypeConverters(InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoItemDao(): TodoItemDao
    abstract fun scheduledItemDao(): ScheduledItemDao
    abstract fun scheduledItemHistoryDao(): ScheduledItemHistoryDao
    abstract fun scheduledItemProcessHistoryDao(): ScheduledItemProcessHistoryDao
}