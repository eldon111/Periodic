package com.emathias.periodic.db.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.emathias.periodic.db.converters.InstantConverter
import com.emathias.periodic.db.entities.ScheduledItem
import com.emathias.periodic.db.entities.TodoItem

@Database(entities = [TodoItem::class, ScheduledItem::class], version = 2, exportSchema = false)
@TypeConverters(InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoItemDao(): TodoItemDao
}